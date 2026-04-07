package org.app.cache.google;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * <pre>
 * - Time-based expiration (TTL)
 * - Access-based expiration
 * - Memory-sensitive caching with soft values
 * - Weak key references
 * - Automatic refresh capabilities
 * - Asynchronous loading
 * - Statistics collection
 * - Removal notifications
 * - Configurable sizes and durations
 * - Thread-safe operations
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleCacheUtils {

    private static final ExecutorService CACHE_EXECUTOR;

    private static final long MAX_SIZE;

    static {
        int processors = Runtime.getRuntime().availableProcessors();
        CACHE_EXECUTOR = new ThreadPoolExecutor(
            processors,                        // Core pool size
            processors << 1,                   // Max pool size
            1000L, TimeUnit.MILLISECONDS,                        // Keep-alive time
            new LinkedBlockingQueue<>(1000),   // Queue capacity
            r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("cache-loader-%d");
                return thread;
            }
        );
        MAX_SIZE = Runtime.getRuntime().maxMemory() / (long) (1024 * 1024 * 10);
    }

    public static <K, V> LoadingCache<K, V> cacheTTL(long timeout, TimeUnit timeUnit, Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(timeout, timeUnit)
            .maximumSize(MAX_SIZE)
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithAccessExpiry(
        long timeout,
        TimeUnit timeUnit,
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .expireAfterAccess(timeout, timeUnit)
            .maximumSize(MAX_SIZE)
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithSoftValues(
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .softValues()
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithWeakKeys(
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .weakKeys()
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithRefresh(
        long duration,
        TimeUnit timeUnit,
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .refreshAfterWrite(duration, timeUnit)
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithAsyncReload(
        long duration,
        TimeUnit timeUnit,
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .refreshAfterWrite(duration, timeUnit)
            .build(com.google.common.cache.CacheLoader.asyncReloading(cacheLoader(function), CACHE_EXECUTOR));
    }

    public static <K, V> LoadingCache<K, V> cacheWithStats(
        long timeout,
        TimeUnit timeUnit,
        Function<K, V> function) {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(timeout, timeUnit)
            .recordStats()
            .build(cacheLoader(function));
    }

    public static <K, V> LoadingCache<K, V> cacheWithRemovalListener(
        long timeout,
        TimeUnit timeUnit,
        Function<K, V> function,
        RemovalListener<K, V> removalListener) {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(timeout, timeUnit)
            .removalListener(removalListener)
            .build(cacheLoader(function));
    }

    public static <K, V> CacheLoader<K, V> cacheLoader(Function<K, V> function) {
        return new CacheLoader<>() {
            @Override
            public V load(K key) {
                return function.apply(key);
            }
        };
    }
}
