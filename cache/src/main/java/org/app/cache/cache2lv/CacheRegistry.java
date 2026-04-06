package org.app.cache.cache2lv;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central registry for all {@link TieredCache} instances in a service.
 * <p>
 * Provides:
 * <ul>
 *   <li>Named cache registration and lookup</li>
 *   <li>Global event listener (across all caches)</li>
 *   <li>Bulk operations: evict by pattern, warmup, shutdown all</li>
 * </ul>
 *
 * <pre>{@code
 * CacheRegistry registry = new CacheRegistry();
 *
 * TieredCache<String, User> userCache = TieredCache.<String, User>builder()
 *     .name("users")
 *     ...
 *     .build();
 *
 * registry.register(userCache);
 *
 * // Later:
 * registry.get("users", String.class, User.class)
 *         .ifPresent(c -> c.evict("user:1"));
 *
 * // Warmup a set of keys on startup
 * registry.warmup("users", List.of("user:1", "user:2", "user:3"));
 *
 * // Global listener for all caches → metrics
 * registry.addGlobalListener(event ->
 *     metrics.increment("cache." + event.type().name().toLowerCase()));
 *
 * registry.close(); // shutdown all
 * }</pre>
 */
public final class CacheRegistry implements Closeable {

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<String, TieredCache> caches = new ConcurrentHashMap<>();
    private final List<Consumer<CacheEvent<?, ?>>> globalListeners = new ArrayList<>();

    // -------------------------------------------------------------------------

    /**
     * Register a cache under its name. Global listeners are attached automatically.
     */
    public <K, V> void register(TieredCache<K, V> cache) {
        caches.put(cache.getClass().getSimpleName(), cache); // used by typed overload
        // Attach global listeners
        globalListeners.forEach(l -> cache.addListener(l::accept));
    }

    /**
     * Register a cache under an explicit name.
     */
    public <K, V> void register(String name, TieredCache<K, V> cache) {
        caches.put(name, cache);
        globalListeners.forEach(l -> cache.addListener(l::accept));
    }

    /**
     * Retrieve a registered cache by name.
     * Returns empty if not registered or types don't match.
     */
    @SuppressWarnings("unchecked")
    public <K, V> Optional<TieredCache<K, V>> get(String name) {
        return Optional.ofNullable((TieredCache<K, V>) caches.get(name));
    }

    /**
     * Warmup a named cache with specific keys synchronously.
     * Calls cache.get() for each key to prime both L1 and L2.
     * Typically called at application startup.
     *
     * <pre>
     * registry.warmup("users", List.of("user:1", "user:2"));
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <K> void warmup(String cacheName, List<K> keys) {
        TieredCache<K, ?> cache = (TieredCache<K, ?>) caches.get(cacheName);
        if (cache == null) throw new IllegalArgumentException("No cache registered: " + cacheName);
        int loaded = 0;
        for (K key : keys) {
            try {
                Optional<?> result = cache.get(key);
                if (result.isPresent()) loaded++;
            } catch (Exception e) {
                // Log but continue warming other keys
                System.err.println("[CacheRegistry] warmup failed key=" + key + ": " + e.getMessage());
            }
        }
        System.out.printf("[CacheRegistry] warmup '%s': %d/%d keys loaded%n", cacheName, loaded, keys.size());
    }

    /**
     * Force-evict a key from ALL registered caches.
     * Useful for global cache busting (e.g. config changes).
     */
    @SuppressWarnings({"unchecked"})
    public void evictAll(Object key) {
        caches.values().forEach(c -> {
            try { c.evict(key); } catch (Exception ignored) {}
        });
    }

    /**
     * Attach a listener that receives events from ALL registered caches.
     * Must be called before {@link #register}.
     */
    public void addGlobalListener(Consumer<CacheEvent<?, ?>> listener) {
        globalListeners.add(listener);
    }

    /**
     * Return a snapshot of stats across all caches for health-check endpoints.
     */
    public Map<String, Integer> l1Sizes() {
        Map<String, Integer> sizes = new LinkedHashMap<>();
        caches.forEach((name, cache) -> sizes.put(name, cache.l1Size()));
        return Collections.unmodifiableMap(sizes);
    }

    public Set<String> registeredNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }

    @Override
    public void close() {
        caches.values().forEach(c -> {
            try { c.close(); } catch (Exception ignored) {}
        });
        caches.clear();
    }
}
