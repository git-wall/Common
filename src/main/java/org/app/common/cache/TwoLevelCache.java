package org.app.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.app.common.thread.ThreadUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@Slf4j
public class TwoLevelCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> localPrimaryCache;
    private final ConcurrentHashMap<K, CacheEntry<V>> localSecondaryCache;
    private final RedisTemplate<K, V> redisCache;
    private final Duration localExpiration;
    private final Duration secondaryExpiration;
    private final ScheduledExecutorService cleanupExecutor;
    private final CacheMode cacheMode;

    public enum CacheMode {
        LOCAL_REDIS,        // Local + Redis combination
        LOCAL_TWO_LEVEL    // Two local caches
    }

    // Constructor for Local + Redis cache
    public TwoLevelCache(Duration localExpiration, RedisTemplate<K, V> redisCache, Duration redisExpiration) {
        this.localPrimaryCache = new ConcurrentHashMap<>(16, 0.75f);
        this.localSecondaryCache = null;
        this.redisCache = redisCache;
        this.localExpiration = localExpiration;
        this.secondaryExpiration = redisExpiration;
        this.cleanupExecutor = ThreadUtils.RuntimeBuilder.scheduledSmallPool();
        this.cacheMode = CacheMode.LOCAL_REDIS;
    }

    // Constructor for two local caches
    public TwoLevelCache(Duration primaryExpiration, Duration secondaryExpiration) {
        this.localPrimaryCache = new ConcurrentHashMap<>(16, 0.75f);
        this.localSecondaryCache = new ConcurrentHashMap<>(16, 0.75f);
        this.redisCache = null;
        this.localExpiration = primaryExpiration;
        this.secondaryExpiration = secondaryExpiration;
        this.cleanupExecutor = ThreadUtils.RuntimeBuilder.scheduledSmallPool();
        this.cacheMode = CacheMode.LOCAL_TWO_LEVEL;
    }

    public V get(K key, Supplier<V> loader) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(loader, "Loader must not be null");

        switch (cacheMode) {
            case LOCAL_REDIS:
                return getFromLocalRedis(key, loader);
            case LOCAL_TWO_LEVEL:
                return getFromTwoLocal(key, loader);
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    private V getFromLocalRedis(K key, Supplier<V> loader) {
        CacheEntry<V> entry = localPrimaryCache.get(key);
        if (entry != null && entry.isExpired()) {
            return entry.getValue();
        }

        V value = redisCache.opsForValue().get(key);
        if (value == null) {
            value = loader.get();
            redisCache.opsForValue().set(key, value, secondaryExpiration);
        }

        localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        return value;
    }

    private V getFromTwoLocal(K key, Supplier<V> loader) {
        CacheEntry<V> entry = localPrimaryCache.get(key);
        if (entry != null && entry.isExpired()) {
            return entry.getValue();
        }

        entry = localSecondaryCache.get(key);
        if (entry != null && entry.isExpired()) {
            V value = entry.getValue();
            localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
            return value;
        }

        V value = loader.get();
        localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        localSecondaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + secondaryExpiration.toMillis()));
        return value;
    }

    public void put(K key, V value) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        switch (cacheMode) {
            case LOCAL_REDIS:
                putLocalRedis(key, value);
                break;
            case LOCAL_TWO_LEVEL:
                putTwoLocal(key, value);
                break;
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    private void putLocalRedis(K key, V value) {
        localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        redisCache.opsForValue().set(key, value, secondaryExpiration);
    }

    private void putTwoLocal(K key, V value) {
        localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        localSecondaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + secondaryExpiration.toMillis()));
    }

    public void update(K key, V value) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "Value must not be null");

        switch (cacheMode) {
            case LOCAL_REDIS:
                updateLocalRedis(key, value);
                break;
            case LOCAL_TWO_LEVEL:
                updateTwoLocal(key, value);
                break;
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    private void updateLocalRedis(K key, V value) {
        if (redisCache.hasKey(key)) {
            redisCache.opsForValue().set(key, value, secondaryExpiration);
            localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        }
    }

    private void updateTwoLocal(K key, V value) {
        if (localSecondaryCache.containsKey(key)) {
            localSecondaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + secondaryExpiration.toMillis()));
            localPrimaryCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + localExpiration.toMillis()));
        }
    }

    public void delete(K key) {
        Assert.notNull(key, "Key must not be null");

        switch (cacheMode) {
            case LOCAL_REDIS:
                deleteLocalRedis(key);
                break;
            case LOCAL_TWO_LEVEL:
                deleteTwoLocal(key);
                break;
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    private void deleteLocalRedis(K key) {
        localPrimaryCache.remove(key);
        redisCache.delete(key);
    }

    private void deleteTwoLocal(K key) {
        localPrimaryCache.remove(key);
        localSecondaryCache.remove(key);
    }

    public void clear() {
        switch (cacheMode) {
            case LOCAL_REDIS:
                clearLocalRedis();
                break;
            case LOCAL_TWO_LEVEL:
                clearTwoLocal();
                break;
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    private void clearLocalRedis() {
        localPrimaryCache.clear();
        // Note: Redis clear is not recommended in production
        // as it might affect other applications
        log.warn("Redis clear operation is not supported to prevent unintended data loss");
    }

    private void clearTwoLocal() {
        localPrimaryCache.clear();
        localSecondaryCache.clear();
    }

    public boolean containsKey(K key) {
        Assert.notNull(key, "Key must not be null");

        switch (cacheMode) {
            case LOCAL_REDIS:
                return localPrimaryCache.containsKey(key) || redisCache.hasKey(key);
            case LOCAL_TWO_LEVEL:
                return localPrimaryCache.containsKey(key) || localSecondaryCache.containsKey(key);
            default:
                throw new IllegalStateException("Unknown cache mode: " + cacheMode);
        }
    }

    public void shutdown() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdown();
        }
    }
    private static class CacheEntry<V> {
        private final V value;
        private final long expirationTime;

        CacheEntry(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        V getValue() {
            return value;
        }

        boolean isExpired() {
            return !isExpired(System.currentTimeMillis());
        }

        boolean isExpired(long now) {
            return now > expirationTime;
        }
    }
}