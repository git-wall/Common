package org.app.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.DataUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * - Cache stampede protection
 * - Stale-while-revalidate
 * - Version-based invalidation
 * - Background refresh
 */
@Slf4j
@Component
public class CacheRecoverCenter {
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private final Map<String, Semaphore> locks = new ConcurrentHashMap<>();
    private final ExecutorService refreshExecutor = Executors.newFixedThreadPool(4);

    /**
     * Lấy dữ liệu từ cache
     *
     * @param key        - Cache key
     * @param ttlMinutes - Thời gian fresh (phút)
     * @param dataLoader - Function load dữ liệu
     */
    public <T> T get(String key, long ttlMinutes, Supplier<T> dataLoader) {
        return get(key, ttlMinutes, null, dataLoader);
    }

    /**
     * Lấy dữ liệu với version check
     *
     * @param key             - Cache key
     * @param ttlMinutes      - Thời gian fresh (phút)
     * @param versionSupplier - Function lấy version từ Hazelcast (có thể null)
     * @param dataLoader      - Function load dữ liệu
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, long ttlMinutes, Supplier<String> versionSupplier, Supplier<T> dataLoader) {
        CacheEntry<?> ce = cache.get(key);
        if (ce == null) {
            return loadWithLock(key, ttlMinutes, versionSupplier, dataLoader, null);
        }

        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);

        Instant now = Instant.now();

        // Case 1: Cache còn FRESH → trả về ngay
        if (entry != null && now.isBefore(entry.freshUntil)) {
            // Kiểm tra version nếu có
            String version = DataUtils.nullable(versionSupplier);
            if (version != null && !version.equals(entry.version)) {
                log.info("Version changed for key={}, refreshing...", key);
                return forceRefresh(key, ttlMinutes, versionSupplier, dataLoader);
            }
            return entry.data;
        }

        // Case 2: Cache STALE (hết fresh nhưng còn trong stale time)
        // → Trả về data cũ NGAY và refresh bất đồng bộ
        if (entry != null && now.isBefore(entry.staleUntil)) {
            // Trigger async refresh (chỉ 1 thread được phép refresh)
            Semaphore lock = locks.computeIfAbsent(key, k -> new Semaphore(1));
            if (lock.tryAcquire()) {
                refreshExecutor.submit(() -> {
                    try {
                        log.debug("Background refresh for key={}", key);
                        loadAndCache(key, ttlMinutes, versionSupplier, dataLoader);
                    } finally {
                        lock.release();
                    }
                });
            }
            // Trả về data cũ ngay không đợi
            return entry.data;
        }

        // Case 3: Cache MISS hoặc quá stale → load đồng bộ
        return loadWithLock(key, ttlMinutes, versionSupplier, dataLoader, entry);
    }

    /**
     * Load dữ liệu với lock (stop cache stampede)
     */
    @SuppressWarnings("unchecked")
    private <T> T loadWithLock(
        String key,
        long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<? extends T> dataLoader,
        CacheEntry<? extends T> oldEntry) {
        Semaphore lock = locks.computeIfAbsent(key, k -> new Semaphore(1));

        // Chỉ 1 thread được load, các thread khác đợi
        try {
            lock.acquire();

            // Double-check: thread khác có thể đã load xong
            CacheEntry<?> recheck = cache.get(key);
            if (recheck != null && Instant.now().isBefore(recheck.freshUntil)) {
                return (T) recheck.data;
            }

            // Load dữ liệu
            return loadAndCache(key, ttlMinutes, versionSupplier, dataLoader);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Fallback về data cũ nếu có
            return oldEntry != null ? oldEntry.data : null;
        } finally {
            lock.release();
        }
    }

    /**
     * Force refresh (use when version changed)
     */
    @SuppressWarnings("unchecked")
    private <T> T forceRefresh(
        String key, long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<T> dataLoader) {
        Semaphore lock = locks.computeIfAbsent(key, k -> new Semaphore(1));

        try {
            lock.acquire();
            return loadAndCache(key, ttlMinutes, versionSupplier, dataLoader);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CacheEntry<?> old = cache.get(key);
            return old != null ? (T) old.data : null;
        } finally {
            lock.release();
        }
    }

    /**
     * Load and save in cache
     */
    @SuppressWarnings("unchecked")
    private <T> T loadAndCache(
        String key, long ttlMinutes,
        Supplier<String> versionSupplier,
        Supplier<T> dataLoader) {
        try {
            // Load dữ liệu mới
            T data = dataLoader.get();
            if (data == null) {
                log.warn("Data loader returned null for key={}", key);
                return null;
            }

            // Lấy version mới
            String version = DataUtils.nullable(versionSupplier);

            // Lưu vào cache với TTL
            Instant now = Instant.now();
            long staleMinutes = ttlMinutes / 2; // Stale time = 50% TTL

            CacheEntry<T> entry = new CacheEntry<>(
                data,
                version,
                now.plusSeconds(ttlMinutes * 60),
                now.plusSeconds((ttlMinutes + staleMinutes) * 60)
            );

            cache.put(key, entry);
            log.debug(
                "Cached key={}, version={}, freshTTL={}min, staleTTL={}min",
                key, version, ttlMinutes, staleMinutes
            );

            return data;

        } catch (Exception e) {
            log.error("Error loading data for key={}", key, e);

            // Fallback về data cũ nếu có
            CacheEntry<?> old = cache.get(key);
            if (old != null) {
                log.warn("Returning stale data due to error for key={}", key);
                return (T) old.data;
            }

            throw new RuntimeException("Failed to load cache for key: " + key, e);
        }
    }

    /**
     * Xóa cache entry
     */
    public void evict(String key) {
        cache.remove(key);
        locks.remove(key);
        log.debug("Evicted cache key={}", key);
    }

    /**
     * Xóa toàn bộ cache
     */
    public void clear() {
        cache.clear();
        locks.clear();
        log.info("Cleared all cache");
    }

    /**
     * Lấy thông tin cache (để monitoring cơ bản)
     */
    public int size() {
        return cache.size();
    }

    @PreDestroy
    public void shutdown() {
        refreshExecutor.shutdown();
        try {
            if (!refreshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                refreshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            refreshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Cache entry chứa data, version và thời gian hết hạn
     */
    @Getter
    @AllArgsConstructor
    private static class CacheEntry<T> {
        private final T data;
        private final String version;        // Version từ Hazelcast (nullable)
        private final Instant freshUntil;    // Thời điểm hết "fresh"
        private final Instant staleUntil;    // Thời điểm hết "stale"
    }
}
