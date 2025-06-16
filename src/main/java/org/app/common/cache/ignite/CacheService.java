package org.app.common.cache.ignite;

import lombok.Getter;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing distributed caches in Apache Ignite.
 * Provides methods to create, configure, and interact with caches.
 */
public class CacheService {

    @Getter
    private final Ignite ignite;
    private final Map<String, IgniteProperties.CacheConfig> cacheConfigs;
    private final Map<String, IgniteCache<Object, Object>> caches = new ConcurrentHashMap<>();

    public CacheService(Ignite ignite, Map<String, IgniteProperties.CacheConfig> cacheConfigs) {
        this.ignite = ignite;
        this.cacheConfigs = cacheConfigs;
        initCaches();
    }

    /**
     * Initialize caches based on configuration.
     */
    private void initCaches() {
        // Create all configured caches
        for (Map.Entry<String, IgniteProperties.CacheConfig> entry : cacheConfigs.entrySet()) {
            createCache(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Creates a cache with the specified name and configuration.
     */
    @SuppressWarnings("unchecked")
    private <K, V> IgniteCache<K, V> createCache(String cacheName, IgniteProperties.CacheConfig config) {
        CacheConfiguration<K, V> cacheCfg = new CacheConfiguration<>(cacheName);

        // Set cache mode
        cacheCfg.setCacheMode(CacheMode.valueOf(config.getCacheMode()));

        // Set atomicity mode
        cacheCfg.setAtomicityMode(CacheAtomicityMode.valueOf(config.getAtomicityMode()));

        // Set write synchronization mode
        cacheCfg.setWriteSynchronizationMode(
                CacheWriteSynchronizationMode.valueOf(config.getWriteSync()));

        // Set backups for partitioned caches
        if (cacheCfg.getCacheMode() == CacheMode.PARTITIONED) {
            cacheCfg.setBackups(config.getBackups());
        }

        // Configure read-through and write-through if needed
        cacheCfg.setReadThrough(config.isReadThrough());
        cacheCfg.setWriteThrough(config.isWriteThrough());

        // Create the cache
        IgniteCache<K, V> cache = ignite.getOrCreateCache(cacheCfg);

        // Store the cache reference
        caches.put(cacheName, (IgniteCache<Object, Object>) cache);

        return cache;
    }

    /**
     * Gets an existing cache or creates a new one with default configuration.
     *
     * @param cacheName The name of the cache
     * @return The IgniteCache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> IgniteCache<K, V> getOrCreateCache(String cacheName) {
        if (caches.containsKey(cacheName)) {
            return (IgniteCache<K, V>) caches.get(cacheName);
        }

        // If cache config exists, use it
        if (cacheConfigs.containsKey(cacheName)) {
            return createCache(cacheName, cacheConfigs.get(cacheName));
        }

        // Create with default configuration
        IgniteCache<K, V> cache = ignite.getOrCreateCache(cacheName);
        caches.put(cacheName, (IgniteCache<Object, Object>) cache);

        return cache;
    }

    /**
     * Gets an existing cache with the specified name.
     *
     * @param cacheName The name of the cache
     * @return The IgniteCache instance or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> IgniteCache<K, V> getCache(String cacheName) {
        return (IgniteCache<K, V>) caches.get(cacheName);
    }

    /**
     * Puts a key-value pair into the specified cache.
     *
     * @param cacheName The name of the cache
     * @param key The key
     * @param value The value
     */
    public <K, V> void put(String cacheName, K key, V value) {
        IgniteCache<K, V> cache = getOrCreateCache(cacheName);
        cache.put(key, value);
    }

    /**
     * Retrieves a value from the specified cache.
     *
     * @param cacheName The name of the cache
     * @param key The key
     * @return The value or null if not found
     */
    public <K, V> V get(String cacheName, K key) {
        IgniteCache<K, V> cache = getOrCreateCache(cacheName);
        return cache.get(key);
    }

    /**
     * Removes an entry from the specified cache.
     *
     * @param cacheName The name of the cache
     * @param key The key to remove
     */
    public <K> void remove(String cacheName, K key) {
        IgniteCache<K, Object> cache = getOrCreateCache(cacheName);
        cache.remove(key);
    }

    /**
     * Clears all entries from the specified cache.
     *
     * @param cacheName The name of the cache to clear
     */
    public void clearCache(String cacheName) {
        IgniteCache<Object, Object> cache = getOrCreateCache(cacheName);
        cache.clear();
    }

    /**
     * Destroys the specified cache.
     *
     * @param cacheName The name of the cache to destroy
     */
    public void destroyCache(String cacheName) {
        IgniteCache<Object, Object> cache = getCache(cacheName);
        if (cache != null) {
            cache.destroy();
            caches.remove(cacheName);
        }
    }

    /**
     * Performs a bulk load operation on the specified cache.
     *
     * @param cacheName The name of the cache
     * @param data The data to load into the cache
     */
    public <K, V> void loadData(String cacheName, Map<K, V> data) {
        IgniteCache<K, V> cache = getOrCreateCache(cacheName);
        cache.putAll(data);
    }

}
