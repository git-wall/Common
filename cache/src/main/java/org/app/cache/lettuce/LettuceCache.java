package org.app.cache.lettuce;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Cache-aside helper built on {@link LettuceJson}.
 * Supports both sync and async get-or-load patterns.
 *
 * <pre>
 * LettuceCache cache = LettuceCache.of(json, 300); // default TTL 5 min
 *
 * // Sync: get from cache, or load from DB and cache result
 * User u = cache.getOrLoad("user:1", User.class, () -> userRepo.findById("1"));
 *
 * // Async: non-blocking get-or-load
 * CompletableFuture&lt;User&gt; fut = cache.getOrLoadAsync("user:1", User.class,
 *     () -> CompletableFuture.supplyAsync(() -> userRepo.findById("1")));
 *
 * // Force invalidate
 * cache.evict("user:1");
 *
 * // Direct put (after DB write)
 * cache.put("user:1", updatedUser);
 *
 * // Reload (evict + load from source)
 * cache.reload("user:1", User.class, () -> userRepo.findById("1"));
 * </pre>
 */
public final class LettuceCache {

    private final LettuceJson json;
    private final long        defaultTtlSeconds;

    private LettuceCache(LettuceJson json, long defaultTtlSeconds) {
        this.json              = json;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public static LettuceCache of(LettuceJson json, long defaultTtlSeconds) {
        return new LettuceCache(json, defaultTtlSeconds);
    }

    // -------------------------------------------------------------------------
    // Sync
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
     * Get from cache or call loader, then cache and return.
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
     * Invalidate then immediately reload from loader.
     */
    public <T> T reload(String key, Class<T> type, Supplier<T> loader) {
        evict(key);
        return getOrLoad(key, type, loader);
    }

    // -------------------------------------------------------------------------
    // Async
    // -------------------------------------------------------------------------

    public <T> CompletableFuture<Void> putAsync(String key, T value) {
        return json.setAsync(key, value, defaultTtlSeconds);
    }

    public <T> CompletableFuture<Optional<T>> getAsync(String key, Class<T> type) {
        return json.getAsync(key, type);
    }

    /**
     * Async get-or-load. Loader is only called on cache miss.
     *
     * <pre>
     * cache.getOrLoadAsync("user:1", User.class,
     *     () -> CompletableFuture.supplyAsync(() -> db.findUser("1")));
     * </pre>
     */
    public <T> CompletableFuture<T> getOrLoadAsync(String key, Class<T> type,
                                                     Supplier<CompletableFuture<T>> asyncLoader) {
        return json.getAsync(key, type).thenCompose(cached ->
            cached
                .<CompletionStage<T>>map(CompletableFuture::completedFuture)
                .orElseGet(() -> asyncLoader.get().thenCompose(value -> {
            if (value == null)
                return CompletableFuture.completedFuture(null);
            return json.setAsync(key, value, defaultTtlSeconds).thenApply(v -> value);
        })));
    }

    public boolean exists(String key) { return json.exists(key); }
}
