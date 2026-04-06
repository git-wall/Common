package org.app.cache.redission;

import org.redisson.api.RMapCache;
import org.redisson.api.map.event.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Cache-aside helper built on Redisson {@link RMapCache}.
 * <p>
 * {@link RMapCache} supports per-entry TTL, idle TTL, and max-size eviction —
 * making it a natural fit for application caching without a separate local cache.
 * <p>
 * For two-level caching (L1 local + L2 Redisson), combine this with
 * the {@code TieredCache} module.
 *
 * <pre>{@code
 * RedissonCache cache = RedissonCache.of(factory, "user-cache",
 *     Duration.ofMinutes(10), // default TTL
 *     5000);                  // max entries (LRU eviction)
 *
 * // Sync get-or-load
 * User u = cache.getOrLoad("user:1", User.class, () -> userRepo.findById("1"));
 *
 * // Async get-or-load
 * cache.getOrLoadAsync("user:1", User.class, () -> CompletableFuture.supplyAsync(() -> db.find("1")));
 *
 * // Put directly (after DB write)
 * cache.put("user:1", updatedUser);
 * cache.put("user:1", updatedUser, Duration.ofMinutes(5));
 *
 * // Evict
 * cache.evict("user:1");
 *
 * // Reload (evict + load from source)
 * cache.reload("user:1", User.class, () -> userRepo.findById("1"));
 *
 * // Listen to eviction events
 * cache.onEvict((key, value, cause) ->
 *     log.info("Evicted: {} reason: {}", key, cause));
 * }</pre>
 *
 * @param <V> value type
 */
public final class RedissonCache<V> {

    private final RMapCache<String, V> map;
    private final Duration             defaultTtl;
    private final Duration             defaultIdleTtl;

    private RedissonCache(RMapCache<String, V> map, Duration defaultTtl, Duration defaultIdleTtl) {
        this.map            = map;
        this.defaultTtl     = defaultTtl;
        this.defaultIdleTtl = defaultIdleTtl;
    }

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Create a cache with TTL and max-size eviction.
     *
     * @param name       Redis key (shared across all instances)
     * @param defaultTtl default time-to-live per entry
     * @param maxSize    max entries (LRU eviction when exceeded; 0 = unlimited)
     */
    public static <V> RedissonCache<V> of(RedissonFactory factory,
                                          String name,
                                          Duration defaultTtl,
                                          int maxSize) {
        RMapCache<String, V> map = factory.client().getMapCache(name);
        if (maxSize > 0) map.setMaxSize(maxSize);
        return new RedissonCache<>(map, defaultTtl, Duration.ZERO);
    }

    /**
     * Create a cache with TTL + idle TTL + max size.
     * Idle TTL evicts entries that haven't been accessed within the window.
     */
    public static <V> RedissonCache<V> of(RedissonFactory factory,
                                          String name,
                                          Duration defaultTtl,
                                          Duration defaultIdleTtl,
                                          int maxSize) {
        RMapCache<String, V> map = factory.client().getMapCache(name);
        if (maxSize > 0) map.setMaxSize(maxSize);
        return new RedissonCache<>(map, defaultTtl, defaultIdleTtl);
    }

    // =========================================================================
    // Core ops
    // =========================================================================

    public void put(String key, V value) {
        put(key, value, defaultTtl);
    }

    public void put(String key, V value, Duration ttl) {
        if (defaultIdleTtl.isZero()) {
            map.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            map.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS,
                defaultIdleTtl.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public void put(String key, V value, Duration ttl, Duration idleTtl) {
        map.put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS,
            idleTtl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** putIfAbsent — only stores if key missing. Returns true if stored. */
    public boolean putIfAbsent(String key, V value) {
        return map.fastPutIfAbsent(key, value, defaultTtl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public Optional<V> get(String key) {
        return Optional.ofNullable(map.get(key));
    }

    public void evict(String key) {
        map.fastRemove(key);
    }

    public boolean exists(String key) {
        return map.containsKey(key);
    }

    public int size() { return map.size(); }

    // =========================================================================
    // Cache-aside patterns
    // =========================================================================

    /**
     * Get from cache, or call loader on miss, cache result, return.
     */
    public V getOrLoad(String key, Class<V> type, Supplier<V> loader) {
        return getOrLoad(key, loader, defaultTtl);
    }

    public V getOrLoad(String key, Supplier<V> loader, Duration ttl) {
        V cached = map.get(key);
        if (cached != null) return cached;
        V value = loader.get();
        if (value != null) put(key, value, ttl);
        return value;
    }

    /**
     * Evict then reload from loader.
     */
    public V reload(String key, Supplier<V> loader) {
        evict(key);
        return getOrLoad(key, loader, defaultTtl);
    }

    // =========================================================================
    // Async
    // =========================================================================

    public CompletableFuture<Void> putAsync(String key, V value) {
        return map.putAsync(key, value, defaultTtl.toMillis(), TimeUnit.MILLISECONDS)
            .toCompletableFuture().thenApply(r -> null);
    }

    public CompletableFuture<Optional<V>> getAsync(String key) {
        return map.getAsync(key).toCompletableFuture().thenApply(Optional::ofNullable);
    }

    public CompletableFuture<V> getOrLoadAsync(String key, Supplier<CompletableFuture<V>> asyncLoader) {
        return map.getAsync(key).toCompletableFuture().thenCompose(cached -> {
            if (cached != null) return CompletableFuture.completedFuture(cached);
            return asyncLoader.get().thenCompose(value -> {
                if (value == null) return CompletableFuture.completedFuture(null);
                return putAsync(key, value).thenApply(v -> value);
            });
        });
    }

    public CompletableFuture<Boolean> evictAsync(String key) {
        return map.fastRemoveAsync(key).toCompletableFuture().thenApply(n -> n > 0);
    }

    // =========================================================================
    // Event listeners (eviction, expiry, update events)
    // =========================================================================

    /**
     * Listen for entry eviction events (expired, max-size eviction, manual remove).
     *
     * <pre>
     * cache.onEvict((key, value, cause) ->
     *     metrics.increment("cache.eviction." + cause.name().toLowerCase()));
     * </pre>
     */
    public void onEvict(EvictListener<V> listener) {
        map.addListener(
            (EntryRemovedListener<String, V>)
                event -> listener.onEvict(event.getKey(), event.getValue(), EvictCause.REMOVED));
        map.addListener(
            (EntryExpiredListener<String, V>)
                event -> listener.onEvict(event.getKey(), event.getValue(), EvictCause.EXPIRED));
    }

    /** Listen for entry creation events. */
    public void onCreated(java.util.function.BiConsumer<String, V> listener) {
        map.addListener((EntryCreatedListener<String, V>) event ->
            listener.accept(event.getKey(), event.getValue()));
    }

    /** Listen for entry update events. */
    public void onUpdated(java.util.function.BiConsumer<String, V> listener) {
        map.addListener((EntryUpdatedListener<String, V>) event ->
            listener.accept(event.getKey(), event.getValue()));
    }

    // =========================================================================
    // Raw access
    // =========================================================================

    public RMapCache<String, V> raw() { return map; }

    // =========================================================================
    // Types
    // =========================================================================

    public enum EvictCause { REMOVED, EXPIRED }

    @FunctionalInterface
    public interface EvictListener<V> {
        void onEvict(String key, V value, EvictCause cause);
    }
}
