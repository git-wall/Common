package org.app.cache.cache2lv;

import java.util.Optional;

/**
 * Strategy for loading a value when both L1 and L2 cache miss.
 * Typically, wraps a database or upstream service call.
 *
 * <pre>{@code
 * CacheLoader<String, User> loader = key -> {
 *     User u = userRepository.findById(key);
 *     return Optional.ofNullable(u);
 * };
 * }</pre>
 *
 * Return {@link Optional#empty()} to indicate "no value exists for this key"
 * (negative caching — prevents repeated loader calls for non-existent keys).
 *
 * @param <K> key type
 * @param <V> value type
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    /**
     * Load value for the given key.
     *
     * @param key the cache key
     * @return the value wrapped in Optional, or empty if not found
     * @throws Exception any error during load; the cache will retry per config
     */
    Optional<V> load(K key) throws Exception;
}
