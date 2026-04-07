package org.app.cache.cache2lv;

import java.time.Duration;
import java.util.Optional;

/**
 * A no-op {@link L2CacheProvider} for testing or when you only need L1.
 * <pre>
 * TieredCache<...> cache = TieredCache.builder()
 *     .l2Provider(NoOpL2Provider.instance())
 *     .build();
 * </pre>
 */
public final class NoOpL2Provider<K, V> implements L2CacheProvider<K, V> {

    @SuppressWarnings("rawtypes")
    private static final NoOpL2Provider INSTANCE = new NoOpL2Provider<>();

    @SuppressWarnings("unchecked")
    public static <K, V> NoOpL2Provider<K, V> instance() { return INSTANCE; }

    @Override public Optional<V> get(K key)                  { return Optional.empty(); }
    @Override public void put(K key, V value, Duration ttl)  { /* no-op */ }
    @Override public void evict(K key)                       { /* no-op */ }
}
