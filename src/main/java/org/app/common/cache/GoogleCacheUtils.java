package org.app.common.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.app.common.thread.ThreadUtils;
import org.springframework.lang.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
public class GoogleCacheUtils {

    private static final ExecutorService CACHE_EXECUTOR;

    private static final long MAX_SIZE;

    static {
        CACHE_EXECUTOR = ThreadUtils.CompileBuilder.logicPool(1000L, TimeUnit.MILLISECONDS);
        MAX_SIZE = Runtime.getRuntime().maxMemory() / (long) (1024 * 1024 * 10);
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheTTL(long timeout, TimeUnit timeUnit, Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, timeUnit)
                .maximumSize(MAX_SIZE)
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithAccessExpiry(
            long timeout,
            TimeUnit timeUnit,
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(timeout, timeUnit)
                .maximumSize(MAX_SIZE)
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithSoftValues(
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .softValues()
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithWeakKeys(
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .weakKeys()
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithRefresh(
            long duration,
            TimeUnit timeUnit,
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(duration, timeUnit)
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithAsyncReload(
            long duration,
            TimeUnit timeUnit,
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(duration, timeUnit)
                .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                }, CACHE_EXECUTOR));
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithStats(
            long timeout,
            TimeUnit timeUnit,
            Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, timeUnit)
                .recordStats()
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheWithRemovalListener(
            long timeout,
            TimeUnit timeUnit,
            Function<K, V> function,
            RemovalListener<K, V> removalListener) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, timeUnit)
                .removalListener(removalListener)
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }
}