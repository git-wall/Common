package org.app.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Local cache with advanced features:
 * - Cache stampede protection (Semaphore-based locking)
 * - Stale-while-revalidate (serve stale + async refresh)
 * - Version-based invalidation (force refresh on version change)
 * - Background refresh with configurable thread pool
 * - Automatic lock cleanup to prevent memory leaks
 * - Basic metrics for monitoring
 */
@Slf4j
@Component
public class CacheRecoverCenter {

    // Core cache storage
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    // Lock per key to prevent cache stampede
    private final Map<String, Semaphore> locks = new ConcurrentHashMap<>();

    // Track last access time for lock cleanup
    private final Map<String, Long> lockLastAccess = new ConcurrentHashMap<>();

    // Background refresh executor
    private final ExecutorService refreshExecutor;

    // Lock cleanup scheduler
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    // Metrics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong staleServed = new AtomicLong(0);
    private final AtomicLong refreshErrors = new AtomicLong(0);

    // Configuration
    private static final long STALE_WINDOW_MINUTES = 60; // Serve stale up to 60 mins after expiry
    private static final long LOCK_CLEANUP_THRESHOLD_MS = 3600_000; // Cleanup locks idle > 1 hour

    public CacheRecoverCenter() {
        // Configurable thread pool with bounded queue
        // ThreadPoolExecutor với bounded queue
        // Max 10 threads, core 2 threads
        // CallerRunsPolicy: queue full → thread will run (backpressure)
        this.refreshExecutor = new ThreadPoolExecutor(
            2, // core threads
            10, // max threads
            60L, TimeUnit.SECONDS, // keep-alive time
            new LinkedBlockingQueue<>(100), // bounded queue
            new ThreadPoolExecutor.CallerRunsPolicy() // fallback policy
        );

        // Schedule lock cleanup every 30 minutes
        cleanupScheduler.scheduleAtFixedRate(this::cleanupIdleLocks, 30, 30, TimeUnit.MINUTES);
    }

    /**
     * Get data from cache with TTL
     */
    public <T> T get(String key, long ttlMinutes, Supplier<T> dataLoader) {
        return get(key, ttlMinutes, null, dataLoader);
    }

    /**
     * Get data from cache with version check
     *
     * @param key             Cache key
     * @param ttlMinutes      Fresh TTL in minutes
     * @param versionSupplier Optional version supplier (null if not needed)
     * @param dataLoader      Function to load fresh data
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, long ttlMinutes, Supplier<String> versionSupplier, Supplier<T> dataLoader) {

        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
        Instant now = Instant.now();

        // === Case 1: Cache miss → load synchronously ===
        if (entry == null) {
            cacheMisses.incrementAndGet();
            return refreshWithLock(key, ttlMinutes, versionSupplier, dataLoader);
        }

        // === Case 2: Cache FRESH → return immediately ===
        if (now.isBefore(entry.freshUntil)) {
            // Check version if provided
            String currentVersion = nullable(versionSupplier);
            if (currentVersion != null && !currentVersion.equals(entry.version)) {
                log.info("[Cache] Version changed for key={}, old={}, new={}",
                    key, entry.version, currentVersion);
                return refreshWithLock(key, ttlMinutes, versionSupplier, dataLoader);
            }

            cacheHits.incrementAndGet();
            return entry.data;
        }

        // === Case 3: Cache STALE but within stale window ===
        if (now.isBefore(entry.staleUntil)) {
            staleServed.incrementAndGet();

            // Try to trigger async refresh (non-blocking)
            triggerAsyncRefresh(key, ttlMinutes, versionSupplier, dataLoader);

            // Return stale immediately
            log.debug("[Cache] Serving stale for key={}, age={}",
                key, Duration.between(entry.freshUntil, now).toMinutes());
            return entry.data;
        }

        // === Case 4: Cache TOO OLD (beyond stale window) → refresh synchronously ===
        log.warn("[Cache] Data too old for key={}, forcing sync refresh", key);
        cacheMisses.incrementAndGet();
        return refreshWithLock(key, ttlMinutes, versionSupplier, dataLoader);
    }

    /**
     * Trigger async refresh without blocking caller
     */
    private <T> void triggerAsyncRefresh(
        String key,
        long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<T> dataLoader) {

        Semaphore lock = getLock(key);

        // Only one thread should refresh at a time
        if (lock.tryAcquire()) {
            refreshExecutor.submit(() -> {
                try {
                    log.debug("[Cache] Async refresh started for key={}", key);
                    refreshAndCache(key, ttlMinutes, versionSupplier, dataLoader);
                } catch (Exception e) {
                    log.error("[Cache] Async refresh failed for key={}", key, e);
                    refreshErrors.incrementAndGet();
                } finally {
                    lock.release();
                }
            });
        } else {
            log.debug("[Cache] Refresh already in progress for key={}", key);
        }
    }

    /**
     * Thread-safe refresh with double-check locking
     */
    @SuppressWarnings("unchecked")
    private <T> T refreshWithLock(
        String key,
        long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<T> dataLoader) {

        Semaphore lock = getLock(key);

        try {
            lock.acquire();

            // === DOUBLE CHECK: Another thread might have refreshed ===
            CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
            Instant now = Instant.now();

            if (entry != null && now.isBefore(entry.freshUntil)) {
                log.debug("[Cache] Double-check: data already fresh for key={}", key);
                return entry.data;
            }

            // Actually refresh
            return refreshAndCache(key, ttlMinutes, versionSupplier, dataLoader);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Cache] Thread interrupted while waiting for lock, key={}", key);

            // Fallback: return stale if available
            CacheEntry<?> old = cache.get(key);
            return old != null ? (T) old.data : null;

        } finally {
            lock.release();
        }
    }

    /**
     * Load fresh data and update cache
     */
    @SuppressWarnings("unchecked")
    private <T> T refreshAndCache(
        String key,
        long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<T> dataLoader) {

        try {
            T data = dataLoader.get();

            if (data == null) {
                log.warn("[Cache] Loader returned null for key={}", key);

                // Keep old data if available
                CacheEntry<?> old = cache.get(key);
                return old != null ? (T) old.data : null;
            }

            Instant now = Instant.now();
            Instant freshUntil = now.plusSeconds(ttlMinutes * 60);
            Instant staleUntil = freshUntil.plusSeconds(STALE_WINDOW_MINUTES * 60);

            CacheEntry<T> entry = new CacheEntry<>(
                data,
                nullable(versionSupplier),
                freshUntil,
                staleUntil
            );

            cache.put(key, entry);

            log.debug("[Cache] Refreshed key={}, freshTTL={}m, staleTTL={}m",
                key, ttlMinutes, STALE_WINDOW_MINUTES);

            return data;

        } catch (Exception e) {
            log.error("[Cache] Failed to refresh key={}", key, e);
            refreshErrors.incrementAndGet();

            // Fallback: return stale data if available
            CacheEntry<?> old = cache.get(key);
            return old != null ? (T) old.data : null;
        }
    }

    /**
     * Get or create lock for a key, update last access time
     */
    private Semaphore getLock(String key) {
        lockLastAccess.put(key, System.currentTimeMillis());
        return locks.computeIfAbsent(key, k -> new Semaphore(1));
    }

    /**
     * Cleanup idle locks to prevent memory leak
     */
    private void cleanupIdleLocks() {
        long now = System.currentTimeMillis();
        int cleaned = 0;

        for (Map.Entry<String, Long> entry : lockLastAccess.entrySet()) {
            String key = entry.getKey();
            long lastAccess = entry.getValue();

            if (now - lastAccess > LOCK_CLEANUP_THRESHOLD_MS) {
                Semaphore lock = locks.get(key);

                // Only remove if not in use
                if (lock != null && lock.availablePermits() == 1) {
                    locks.remove(key);
                    lockLastAccess.remove(key);
                    cleaned++;
                }
            }
        }

        if (cleaned > 0) {
            log.info("[Cache] Cleaned up {} idle locks", cleaned);
        }
    }

    /**
     * Evict specific key
     */
    public void evict(String key) {
        cache.remove(key);
        locks.remove(key);
        lockLastAccess.remove(key);
        log.debug("[Cache] Evicted key={}", key);
    }

    /**
     * Clear all cache
     */
    public void clear() {
        cache.clear();
        locks.clear();
        lockLastAccess.clear();
        log.info("[Cache] Cleared all cache entries");
    }

    /**
     * Get cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Get cache metrics
     */
    public CacheMetrics getMetrics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;

        return new CacheMetrics(
            cache.size(),
            locks.size(),
            hits,
            misses,
            staleServed.get(),
            refreshErrors.get(),
            hitRate
        );
    }

    /**
     * Reset metrics
     */
    public void resetMetrics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        staleServed.set(0);
        refreshErrors.set(0);
        log.info("[Cache] Metrics reset");
    }

    @PreDestroy
    public void shutdown() {
        log.info("[Cache] Shutting down executors...");

        // Shutdown refresh executor
        refreshExecutor.shutdown();
        try {
            if (!refreshExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                refreshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            refreshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Shutdown cleanup scheduler
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[Cache] Shutdown complete");
    }

    // === Helper methods ===

    private static <T> T nullable(Supplier<T> supplier) {
        return supplier == null ? null : supplier.get();
    }

    // === Data classes ===

    @Getter
    @AllArgsConstructor
    private static class CacheEntry<T> {
        private final T data;
        private final String version;
        private final Instant freshUntil;   // Data is fresh until this time
        private final Instant staleUntil;   // Data can be served as stale until this time
    }

    @Getter
    @AllArgsConstructor
    public static class CacheMetrics {
        private final int cacheSize;
        private final int lockCount;
        private final long cacheHits;
        private final long cacheMisses;
        private final long staleServed;
        private final long refreshErrors;
        private final double hitRatePercent;

        @Override
        public String toString() {
            return String.format(
                "CacheMetrics[size=%d, locks=%d, hits=%d, misses=%d, stale=%d, errors=%d, hitRate=%.2f%%]",
                cacheSize, lockCount, cacheHits, cacheMisses, staleServed, refreshErrors, hitRatePercent
            );
        }
    }
}
