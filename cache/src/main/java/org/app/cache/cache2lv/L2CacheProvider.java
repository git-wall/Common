package org.app.cache.cache2lv;

import java.time.Duration;
import java.util.Optional;

/**
 * Abstraction over any distributed cache backend (Redis, Memcached, custom service, etc).
 * <p>
 * Implementations must handle serialization/deserialization internally.
 * The {@link TieredCache} calls this interface without knowing the underlying transport.
 *
 * <pre>
 * // Wire it up:
 * L2CacheProvider<String, User> l2 = new RedisL2Provider<>(redisJson, "users");
 * </pre>
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface L2CacheProvider<K, V> {

    /**
     * Retrieve a value from the distributed cache.
     * @return the value, or empty if not present / expired
     */
    Optional<V> get(K key);

    /**
     * Store a value in the distributed cache with a TTL.
     */
    void put(K key, V value, Duration ttl);

    /**
     * Explicitly remove a key from the distributed cache.
     * Used during manual invalidation.
     */
    void evict(K key);

    /**
     * Optional: broadcast an invalidation event to all instances.
     * Default is no-op. Override for pub/sub based invalidation.
     */
    default void broadcastInvalidation(K key) { /* no-op */ }
}
