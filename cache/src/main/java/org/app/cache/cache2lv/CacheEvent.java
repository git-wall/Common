package org.app.cache.cache2lv;

import java.time.Instant;

/**
 * Immutable event emitted by {@link TieredCache} for observability.
 * Subscribe via {@link TieredCache#addListener}.
 *
 * <pre>
 * cache.addListener(event -> {
 *     metrics.increment("cache.event." + event.type().name().toLowerCase(),
 *                        "cache", event.cacheName());
 * });
 * </pre>
 */
public final class CacheEvent<K, V> {

    public enum Type {
        L1_HIT,           // served from local memory
        L2_HIT,           // served from distributed cache
        MISS,             // both L1 and L2 missed → loader called
        LOADED,           // loader returned value successfully
        LOAD_FAILED,      // loader failed all retries
        STALE_SERVED,     // served stale value after load failure
        EVICTED,          // key explicitly evicted
        PUT,              // value pushed directly via put()
        BACKGROUND_REFRESH_STARTED,
        L2_ERROR,         // L2 call failed (degraded to loader)
        STAMPEDE_TIMEOUT, // thread waited too long for lock
    }

    private final Type    type;
    private final String  cacheName;
    private final K       key;
    private final V       value;      // null for MISS/EVICTED/LOAD_FAILED
    private final int     attempt;    // for LOADED: which retry succeeded (1-based)
    private final boolean isManualRefresh;
    private final Throwable error;    // for LOAD_FAILED/L2_ERROR
    private final Instant  timestamp;

    private CacheEvent(Type type, String cacheName, K key, V value,
                       int attempt, boolean isManualRefresh, Throwable error) {
        this.type            = type;
        this.cacheName       = cacheName;
        this.key             = key;
        this.value           = value;
        this.attempt         = attempt;
        this.isManualRefresh = isManualRefresh;
        this.error           = error;
        this.timestamp       = Instant.now();
    }

    // --- Factory methods ---

    static <K, V> CacheEvent<K, V> l1Hit(String name, K key, V value) {
        return new CacheEvent<>(Type.L1_HIT, name, key, value, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> l2Hit(String name, K key, V value) {
        return new CacheEvent<>(Type.L2_HIT, name, key, value, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> miss(String name, K key) {
        return new CacheEvent<>(Type.MISS, name, key, null, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> loaded(String name, K key, V value, int attempt, boolean manual) {
        return new CacheEvent<>(Type.LOADED, name, key, value, attempt, manual, null);
    }

    static <K, V> CacheEvent<K, V> loadFailed(String name, K key, Throwable error) {
        return new CacheEvent<>(Type.LOAD_FAILED, name, key, null, 0, false, error);
    }

    static <K, V> CacheEvent<K, V> staleServed(String name, K key, V value) {
        return new CacheEvent<>(Type.STALE_SERVED, name, key, value, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> evicted(String name, K key) {
        return new CacheEvent<>(Type.EVICTED, name, key, null, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> put(String name, K key, V value) {
        return new CacheEvent<>(Type.PUT, name, key, value, 0, false, null);
    }

    static <K, V> CacheEvent<K, V> l2Error(String name, K key, Throwable error) {
        return new CacheEvent<>(Type.L2_ERROR, name, key, null, 0, false, error);
    }

    static <K, V> CacheEvent<K, V> stampedeTimeout(String name, K key) {
        return new CacheEvent<>(Type.STAMPEDE_TIMEOUT, name, key, null, 0, false, null);
    }

    // --- Accessors ---

    public Type      type()            { return type; }
    public String    cacheName()       { return cacheName; }
    public K         key()             { return key; }
    public V         value()           { return value; }
    public int       attempt()         { return attempt; }
    public boolean   isManualRefresh() { return isManualRefresh; }
    public Throwable error()           { return error; }
    public Instant   timestamp()       { return timestamp; }

    @Override
    public String toString() {
        return String.format("CacheEvent[%s cache=%s key=%s attempt=%d manual=%s]",
            type, cacheName, key, attempt, isManualRefresh);
    }
}
