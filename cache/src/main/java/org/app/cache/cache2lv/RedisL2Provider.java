package org.app.cache.cache2lv;


import org.app.cache.jedis.RedisJson;
import org.app.cache.jedis.RedisOps;

import java.time.Duration;
import java.util.Optional;

/**
 * L2 provider backed by Redis, using {@link RedisJson} (with LZ4 compression).
 * <p>
 * Keys are namespaced: {@code {namespace}:{key}}.
 *
 * <pre>
 * L2CacheProvider<String, User> l2 =
 *     RedisL2Provider.of(redisJson, "users", User.class);
 * </pre>
 *
 * @param <K> key type (must have meaningful toString())
 * @param <V> value type (must be Jackson-serializable)
 */
public final class RedisL2Provider<K, V> implements L2CacheProvider<K, V> {

    private static final String INVALIDATION_CHANNEL_PREFIX = "__cache_invalidate__:";

    private final RedisJson json;
    private final RedisOps ops;
    private final String namespace;
    private final Class<V> valueType;

    private RedisL2Provider(RedisJson json, RedisOps ops, String namespace, Class<V> valueType) {
        this.json = json;
        this.ops = ops;
        this.namespace = namespace;
        this.valueType = valueType;
    }

    public static <K, V> RedisL2Provider<K, V> of(RedisJson json, RedisOps ops,
                                                  String namespace, Class<V> valueType) {
        return new RedisL2Provider<>(json, ops, namespace, valueType);
    }

    // -------------------------------------------------------------------------

    @Override
    public Optional<V> get(K key) {
        return json.get(redisKey(key), valueType);
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        json.set(redisKey(key), value, ttl.getSeconds());
    }

    @Override
    public void evict(K key) {
        ops.del(redisKey(key));
    }

    /**
     * Publish invalidation event to a Redis pub/sub channel so all instances
     * listening can evict their L1 for this key.
     * Subscribe with {@link #subscribeInvalidations}.
     */
    @Override
    public void broadcastInvalidation(K key) {
        // Redis PUBLISH — fire and forget
        try (redis.clients.jedis.Jedis j = ops.getFactory().getResource()) {
            j.publish(channel(), key.toString());
        }
    }

    /**
     * Start a background subscriber thread that listens for remote invalidation
     * events and calls {@code onInvalidate} for each key received.
     * <p>
     * Call this once at startup if you want cross-instance L1 invalidation.
     */
    public void subscribeInvalidations(java.util.function.Consumer<String> onInvalidate) {
        Thread t = new Thread(() -> {
            try (redis.clients.jedis.Jedis j = ops.getFactory().getResource()) {
                j.subscribe(new redis.clients.jedis.JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        onInvalidate.accept(message);
                    }
                }, channel());
            }
        }, "cache-invalidate-sub-" + namespace);
        t.setDaemon(true);
        t.start();
    }

    // -------------------------------------------------------------------------

    private String redisKey(K key) {
        return namespace + ":" + key;
    }

    private String channel() {
        return INVALIDATION_CHANNEL_PREFIX + namespace;
    }
}
