package org.app.cache.jedis;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thin cache-aside helper built on {@link RedisJson}.
 * <p>
 * Automatically loads from source when key is missing (cache-miss pattern).
 *
 * <pre>{@code
 * RedisCache cache = RedisCache.of(redisJson, 300); // default 5-min TTL
 *
 * // Get or load
 * User user = cache.getOrLoad("user:1", User.class, () -> userRepo.findById(1));
 *
 * // Explicit store / invalidate
 * cache.put("user:1", user);
 * cache.evict("user:1");
 * }</pre>
 */
public final class RedisCache {

    private final RedisJson json;
    private final long defaultTtlSeconds;

    private RedisCache(RedisJson json, long defaultTtlSeconds) {
        this.json = json;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public static RedisCache of(RedisJson json, long defaultTtlSeconds) {
        return new RedisCache(json, defaultTtlSeconds);
    }

    // -------------------------------------------------------------------------

    public <T> void put(String key, T value) {
        json.set(key, value, defaultTtlSeconds);
    }

    public <T> void put(String key, T value, long ttlSeconds) {
        json.set(key, value, ttlSeconds);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return json.get(key, type);
    }

    public void evict(String key) {
        json.delete(key);
    }

    /**
     * Return cached value if present, otherwise call {@code loader},
     * store the result, and return it.
     */
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(key, type, loader, defaultTtlSeconds);
    }

    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader, long ttlSeconds) {
        Optional<T> cached = json.get(key, type);
        if (cached.isPresent()) return cached.get();
        T value = loader.get();
        if (value != null) json.set(key, value, ttlSeconds);
        return value;
    }

    /**
     * Invalidate then reload immediately.
     */
    public <T> T reload(String key, Class<T> type, Supplier<T> loader) {
        evict(key);
        return getOrLoad(key, type, loader);
    }
}
