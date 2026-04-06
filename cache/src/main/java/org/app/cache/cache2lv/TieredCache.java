package org.app.cache.cache2lv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Two-level tiered cache with:
 * <ul>
 *   <li><b>L1</b>: in-process {@link ConcurrentHashMap} — nanosecond access</li>
 *   <li><b>L2</b>: distributed cache via {@link L2CacheProvider} (Redis, custom...)</li>
 *   <li><b>Anti-stampede</b>: per-key mutex prevents thundering herd on cache miss</li>
 *   <li><b>Proactive refresh</b>: background thread refreshes entries before TTL expires</li>
 *   <li><b>Stale-while-revalidate</b>: serves stale L1 data while refresh runs in background</li>
 *   <li><b>Loader retry</b>: configurable retries with delay on loader failure</li>
 *   <li><b>Stale fallback</b>: returns stale value on error if {@code allowStaleOnError=true}</li>
 *   <li><b>Manual refresh/invalidate</b>: dev can push new data or evict any key</li>
 *   <li><b>Event hooks</b>: listen to hit/miss/refresh/error events for metrics</li>
 * </ul>
 *
 * <pre>{@code
 * TieredCache<String, User> cache = TieredCache.<String, User>builder()
 *     .name("users")
 *     .config(CacheConfig.builder().ttl(Duration.ofMinutes(5)).build())
 *     .loader(key -> Optional.ofNullable(userRepo.findById(key)))
 *     .l2Provider(RedisL2Provider.of(json, ops, "users", User.class))
 *     .build();
 *
 * // Read
 * Optional<User> user = cache.get("user:1");
 *
 * // Write-through (after DB update)
 * cache.refresh("user:1");   // trigger reload from DB immediately
 * cache.put("user:1", updated); // push known value directly, no DB call
 * cache.evict("user:1");     // invalidate everywhere
 * }</pre>
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class TieredCache<K, V> implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(TieredCache.class);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final String                           name;
    private final CacheConfig                      config;
    private final CacheLoader<K, V>                loader;
    private final L2CacheProvider<K, V>            l2;

    /** L1: the actual in-process store */
    private final ConcurrentHashMap<K, CacheEntry<V>> l1 = new ConcurrentHashMap<>();

    /**
     * Per-key locks for anti-stampede: when L1+L2 both miss, only ONE thread
     * calls the loader; all others wait on the same lock.
     */
    private final ConcurrentHashMap<K, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    /** Pending stampede results so waiting threads don't reload again */
    private final ConcurrentHashMap<K, CompletableFuture<Optional<V>>> pendingLoads =
        new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;
    private final ExecutorService          refreshExecutor;

    private final List<Consumer<CacheEvent<K, V>>> eventListeners = new CopyOnWriteArrayList<>();

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    private TieredCache(Builder<K, V> b) {
        this.name            = b.name;
        this.config          = b.config;
        this.loader          = b.loader;
        this.l2              = b.l2Provider;

        this.refreshExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "cache-refresh-" + name);
            t.setDaemon(true);
            return t;
        });

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-maintenance-" + name);
            t.setDaemon(true);
            return t;
        });

        long intervalMs = config.getMaintenanceInterval().toMillis();
        scheduler.scheduleAtFixedRate(this::runMaintenance,
            intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Primary read. Follows chain: L1 → L2 → loader (with retry).
     * Triggers background refresh if entry is within refresh lead window.
     * Returns stale data if {@code allowStaleOnError=true} and all sources fail.
     */
    public Optional<V> get(K key) {
        // --- L1 hit ---
        CacheEntry<V> entry = l1.get(key);
        if (entry != null && !entry.isHardExpired()) {
            emit(CacheEvent.l1Hit(name, key, entry.value));
            triggerBackgroundRefreshIfNeeded(key, entry);
            return Optional.ofNullable(entry.value);
        }

        // --- Stale entry exists (beyond hard TTL but within grace period) ---
        CacheEntry<V> staleEntry = entry; // keep ref for fallback

        // --- Anti-stampede: serialize loader calls for same key ---
        return loadWithStampedeProtection(key, staleEntry);
    }

    /**
     * Immediately invalidate key in L1, L2, and broadcast to other instances.
     */
    public void evict(K key) {
        l1.remove(key);
        if (config.isL2Enabled() && l2 != null) {
            l2.evict(key);
            l2.broadcastInvalidation(key);
        }
        emit(CacheEvent.evicted(name, key));
        log.debug("[{}] evicted key={}", name, key);
    }

    /**
     * Force-reload from loader and update both L1 and L2.
     * Useful after a DB write: {@code cache.refresh("user:1")}.
     */
    public Optional<V> refresh(K key) {
        log.debug("[{}] manual refresh key={}", name, key);
        return loadFromSource(key, null, true);
    }

    /**
     * Push a known value directly into L1 and L2 without calling loader.
     * Useful when you already have the fresh value (e.g. after a DB save).
     * <pre>
     * userRepo.save(user);
     * cache.put("user:" + user.getId(), user);
     * </pre>
     */
    public void put(K key, V value) {
        storeInL1(key, value);
        if (config.isL2Enabled() && l2 != null) {
            l2.put(key, value, config.getL2Ttl());
        }
        emit(CacheEvent.put(name, key, value));
        log.debug("[{}] put key={}", name, key);
    }

    /**
     * Add an event listener for observability (metrics, logging, alerts).
     * <pre>
     * cache.addListener(event -> metrics.increment("cache." + event.type()));
     * </pre>
     */
    public void addListener(Consumer<CacheEvent<K, V>> listener) {
        eventListeners.add(listener);
    }

    /** Current number of L1 entries (includes entries near expiry). */
    public int l1Size() { return l1.size(); }

    /** Wipe the entire L1 local store. Does NOT touch L2. */
    public void clearLocal() { l1.clear(); }

    // -------------------------------------------------------------------------
    // Core load logic
    // -------------------------------------------------------------------------

    private Optional<V> loadWithStampedeProtection(K key, CacheEntry<V> staleEntry) {
        // Use per-key lock to ensure only ONE thread calls L2+loader
        ReentrantLock lock = keyLocks.computeIfAbsent(key, k -> new ReentrantLock());

        boolean locked;
        try {
            locked = lock.tryLock(config.getStampedeLockTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return staleOrThrow(key, staleEntry, new RuntimeException("Interrupted waiting for cache lock", e));
        }

        if (!locked) {
            // Timeout: serve stale or throw
            log.warn("[{}] stampede lock timeout key={}, serving stale={}", name, key, staleEntry != null);
            emit(CacheEvent.stampedeTimeout(name, key));
            return staleEntry != null ? Optional.ofNullable(staleEntry.value) : Optional.empty();
        }

        try {
            // Double-check: another thread may have just loaded it
            CacheEntry<V> recheck = l1.get(key);
            if (recheck != null && !recheck.isHardExpired()) {
                emit(CacheEvent.l1Hit(name, key, recheck.value));
                return Optional.ofNullable(recheck.value);
            }

            // --- L2 check ---
            if (config.isL2Enabled() && l2 != null) {
                try {
                    Optional<V> l2Value = l2.get(key);
                    if (l2Value.isPresent()) {
                        storeInL1(key, l2Value.get()); // warm up L1 from L2
                        emit(CacheEvent.l2Hit(name, key, l2Value.get()));
                        return l2Value;
                    }
                } catch (Exception e) {
                    log.warn("[{}] L2 get failed key={}: {}", name, key, e.getMessage());
                    emit(CacheEvent.l2Error(name, key, e));
                }
            }

            // --- Miss: call loader ---
            emit(CacheEvent.miss(name, key));
            return loadFromSource(key, staleEntry, false);

        } finally {
            lock.unlock();
            keyLocks.remove(key, lock);
        }
    }

    private Optional<V> loadFromSource(K key, CacheEntry<V> staleEntry, boolean isManualRefresh) {
        Exception lastException = null;
        int attempts = config.getLoaderRetries() + 1;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                Optional<V> result = loader.load(key);

                // Negative cache: loader returned empty (key doesn't exist)
                if (result.isEmpty()) {
                    // Store a null marker briefly to prevent repeated DB hits
                    storeNullMarkerInL1(key);
                    return Optional.empty();
                }

                V value = result.get();
                storeInL1(key, value);
                if (config.isL2Enabled() && l2 != null) {
                    try { l2.put(key, value, config.getL2Ttl()); }
                    catch (Exception e) {
                        log.warn("[{}] L2 put failed key={}: {}", name, key, e.getMessage());
                    }
                }
                emit(CacheEvent.loaded(name, key, value, attempt, isManualRefresh));
                return Optional.of(value);

            } catch (Exception e) {
                lastException = e;
                log.warn("[{}] loader attempt {}/{} failed key={}: {}", name, attempt, attempts, key, e.getMessage());

                if (attempt < attempts) {
                    try { Thread.sleep(config.getLoaderRetryDelay().toMillis()); }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        emit(CacheEvent.loadFailed(name, key, lastException));
        return staleOrThrow(key, staleEntry, lastException);
    }

    private Optional<V> staleOrThrow(K key, CacheEntry<V> staleEntry, Exception cause) {
        if (config.isAllowStaleOnError() && staleEntry != null && isWithinGracePeriod(staleEntry)) {
            log.warn("[{}] serving stale value for key={}", name, key);
            emit(CacheEvent.staleServed(name, key, staleEntry.value));
            return Optional.ofNullable(staleEntry.value);
        }
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw new CacheLoadException("Failed to load cache key: " + key, cause);
    }

    // -------------------------------------------------------------------------
    // Proactive refresh
    // -------------------------------------------------------------------------

    private void triggerBackgroundRefreshIfNeeded(K key, CacheEntry<V> entry) {
        if (config.getRefreshLeadTime().isZero()) return;
        if (!entry.needsRefresh()) return;
        if (!entry.markRefreshing()) return; // another thread already scheduled

        refreshExecutor.submit(() -> {
            log.debug("[{}] background refresh key={}", name, key);
            try {
                loadFromSource(key, entry, false);
            } catch (Exception e) {
                log.warn("[{}] background refresh failed key={}: {}", name, key, e.getMessage());
                entry.state = CacheEntry.State.STALE;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Maintenance (eviction + proactive scan)
    // -------------------------------------------------------------------------

    private void runMaintenance() {
        int evicted = 0;
        int scheduled = 0;

        for (Map.Entry<K, CacheEntry<V>> e : l1.entrySet()) {
            CacheEntry<V> entry = e.getValue();

            if (isExpiredBeyondGrace(entry)) {
                l1.remove(e.getKey());
                evicted++;
                continue;
            }

            if (entry.needsRefresh()) {
                triggerBackgroundRefreshIfNeeded(e.getKey(), entry);
                scheduled++;
            }
        }

        // Enforce max size: evict oldest (approximate, no strict ordering needed)
        if (l1.size() > config.getLocalMaxSize()) {
            int toEvict = l1.size() - config.getLocalMaxSize();
            l1.keySet().stream().limit(toEvict).forEach(l1::remove);
        }

        if (evicted > 0 || scheduled > 0) {
            log.debug("[{}] maintenance: evicted={}, refresh_scheduled={}, l1_size={}",
                name, evicted, scheduled, l1.size());
        }
    }

    // -------------------------------------------------------------------------
    // L1 storage helpers
    // -------------------------------------------------------------------------

    private void storeInL1(K key, V value) {
        Instant now = Instant.now();
        Duration ttl = config.getTtl();
        Duration lead = config.getRefreshLeadTime();

        Instant expiresAt    = now.plus(ttl);
        Instant refreshAfter = lead.isZero() ? expiresAt : now.plus(ttl).minus(lead);

        l1.put(key, new CacheEntry<>(value, expiresAt, refreshAfter));
    }

    /** Null marker: store null value with very short TTL to prevent negative stampede */
    private void storeNullMarkerInL1(K key) {
        Instant now = Instant.now();
        Duration shortTtl = Duration.ofSeconds(30);
        l1.put(key, new CacheEntry<>(null, now.plus(shortTtl), now.plus(shortTtl)));
    }

    private boolean isWithinGracePeriod(CacheEntry<V> entry) {
        return Instant.now().isBefore(entry.expiresAt.plus(config.getStaleGracePeriod()));
    }

    private boolean isExpiredBeyondGrace(CacheEntry<V> entry) {
        return Instant.now().isAfter(entry.expiresAt.plus(config.getStaleGracePeriod()));
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    private void emit(CacheEvent<K, V> event) {
        for (Consumer<CacheEvent<K, V>> l : eventListeners) {
            try { l.accept(event); }
            catch (Exception e) { /* never let listeners kill cache operations */ }
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        scheduler.shutdown();
        refreshExecutor.shutdown();
        l1.clear();
        log.info("[{}] cache closed", name);
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static final class Builder<K, V> {
        private String                name;
        private CacheConfig           config  = CacheConfig.builder().build();
        private CacheLoader<K, V>     loader;
        private L2CacheProvider<K, V> l2Provider;

        public Builder<K, V> name(String name) { this.name = name; return this; }
        public Builder<K, V> config(CacheConfig c) { this.config = c; return this; }
        public Builder<K, V> loader(CacheLoader<K, V> loader) { this.loader = loader; return this; }
        public Builder<K, V> l2Provider(L2CacheProvider<K, V> l2) { this.l2Provider = l2; return this; }

        public TieredCache<K, V> build() {
            Objects.requireNonNull(name,   "name is required");
            Objects.requireNonNull(loader, "loader is required");
            return new TieredCache<>(this);
        }
    }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    public static final class CacheLoadException extends RuntimeException {
        private static final long serialVersionUID = 9107538518561283855L;

        public CacheLoadException(String msg, Throwable cause) { super(msg, cause); }
    }
}
