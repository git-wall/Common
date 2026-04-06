package org.app.cache.lettuce;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.app.cache.Compression;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Async Lettuce Redis operations — all methods return {@link CompletableFuture}.
 * <p>
 * Lettuce's async API is non-blocking and backed by Netty — perfect for reactive
 * pipelines, parallel fetches, and high-throughput scenarios.
 *
 * <pre>{@code
 * LettuceAsyncOps async = LettuceAsyncOps.of(factory);
 *
 * // Fire and forget
 * async.set("key", "value").thenAccept(r -> log.info("set: {}", r));
 *
 * // Await result
 * String val = async.get("key").join();
 *
 * // Parallel fetch
 * CompletableFuture&lt;Optional&lt;String&gt;&gt; f1 = async.get("k1");
 * CompletableFuture&lt;Optional&lt;String&gt;&gt; f2 = async.get("k2");
 * CompletableFuture.allOf(f1, f2).join();
 *
 * // Pipeline batch — all fired in one flush, no per-command roundtrip
 * List&lt;CompletableFuture&lt;?&gt;&gt; futures = async.batch(cmds -> List.of(
 *     cmds.set("a","1"),
 *     cmds.set("b","2"),
 *     cmds.incr("counter")
 * ));
 * CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
 * }</pre>
 */
public final class LettuceAsyncOps {

    private final LettuceClientFactory factory;
    private final boolean              compressionEnabled;
    private final int                  compressionThreshold;

    private LettuceAsyncOps(LettuceClientFactory factory) {
        this.factory              = factory;
        this.compressionEnabled   = factory.getConfig().isCompressionEnabled();
        this.compressionThreshold = factory.getConfig().getCompressionThresholdBytes();
    }

    public static LettuceAsyncOps of(LettuceClientFactory factory) {
        return new LettuceAsyncOps(factory);
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private RedisClusterAsyncCommands<String, String> async() {
        return factory.async();
    }

    private RedisClusterAsyncCommands<byte[], byte[]> binaryAsync() {
        return factory.binaryAsync();
    }

    private byte[] kb(String key) { return key.getBytes(StandardCharsets.UTF_8); }
    private byte[] vb(String val) { return val.getBytes(StandardCharsets.UTF_8); }
    private String vs(byte[] b)   { return b == null ? null : new String(b, StandardCharsets.UTF_8); }

    private byte[] compress(byte[] raw) {
        if (!compressionEnabled || raw == null || raw.length < compressionThreshold) return raw;
        return Compression.compress(raw);
    }

    private byte[] decompress(byte[] stored) {
        if (!compressionEnabled || stored == null) return stored;
        return Compression.decompress(stored);
    }

    private <T> CompletableFuture<T> cf(RedisFuture<T> f) {
        return f.toCompletableFuture();
    }

    // -------------------------------------------------------------------------
    // String
    // -------------------------------------------------------------------------

    public CompletableFuture<String> set(String key, String value) {
        return cf(binaryAsync().set(kb(key), compress(vb(value))));
    }

    public CompletableFuture<String> set(String key, String value, long ttlSeconds) {
        return cf(binaryAsync().setex(kb(key), ttlSeconds, compress(vb(value))));
    }

    public CompletableFuture<Boolean> setNx(String key, String value) {
        return cf(binaryAsync().setnx(kb(key), compress(vb(value))));
    }

    public CompletableFuture<Boolean> setNx(String key, String value, long ttlSeconds) {
        return cf(binaryAsync().set(kb(key), compress(vb(value)),
            io.lettuce.core.SetArgs.Builder.nx().ex(ttlSeconds)))
            .thenApply("OK"::equals);
    }

    public CompletableFuture<Optional<String>> get(String key) {
        return cf(binaryAsync().get(kb(key)))
            .thenApply(raw -> Optional.ofNullable(vs(decompress(raw))));
    }

    public CompletableFuture<Long> del(String... keys) {
        byte[][] bkeys = Arrays.stream(keys).map(this::kb).toArray(byte[][]::new);
        return cf(binaryAsync().del(bkeys));
    }

    public CompletableFuture<Boolean> exists(String key) {
        return cf(binaryAsync().exists(kb(key))).thenApply(n -> n > 0);
    }

    public CompletableFuture<Boolean> expire(String key, long ttlSeconds) {
        return cf(binaryAsync().expire(kb(key), ttlSeconds));
    }

    public CompletableFuture<Long> ttl(String key) {
        return cf(binaryAsync().ttl(kb(key)));
    }

    public CompletableFuture<Long> incr(String key)           { return cf(async().incr(key)); }
    public CompletableFuture<Long> incrBy(String key, long d) { return cf(async().incrby(key, d)); }
    public CompletableFuture<Long> decr(String key)           { return cf(async().decr(key)); }

    // -------------------------------------------------------------------------
    // Binary
    // -------------------------------------------------------------------------

    public CompletableFuture<String> setBinary(String key, byte[] data) {
        return cf(binaryAsync().set(kb(key), compress(data)));
    }

    public CompletableFuture<String> setBinary(String key, byte[] data, long ttlSeconds) {
        return cf(binaryAsync().setex(kb(key), ttlSeconds, compress(data)));
    }

    public CompletableFuture<byte[]> getBinary(String key) {
        return cf(binaryAsync().get(kb(key))).thenApply(this::decompress);
    }

    // -------------------------------------------------------------------------
    // Hash
    // -------------------------------------------------------------------------

    public CompletableFuture<Boolean> hset(String key, String field, String value) {
        return cf(async().hset(key, field, value));
    }

    public CompletableFuture<Long> hsetAll(String key, Map<String, String> fields) {
        return cf(async().hset(key, fields));
    }

    public CompletableFuture<Optional<String>> hget(String key, String field) {
        return cf(async().hget(key, field)).thenApply(Optional::ofNullable);
    }

    public CompletableFuture<Map<String, String>> hgetAll(String key) {
        return cf(async().hgetall(key));
    }

    public CompletableFuture<Long> hdel(String key, String... fields) {
        return cf(async().hdel(key, fields));
    }

    // -------------------------------------------------------------------------
    // List
    // -------------------------------------------------------------------------

    public CompletableFuture<Long> lpush(String key, String... values) { return cf(async().lpush(key, values)); }
    public CompletableFuture<Long> rpush(String key, String... values) { return cf(async().rpush(key, values)); }
    public CompletableFuture<Optional<String>> lpop(String key) { return cf(async().lpop(key)).thenApply(Optional::ofNullable); }
    public CompletableFuture<Optional<String>> rpop(String key) { return cf(async().rpop(key)).thenApply(Optional::ofNullable); }
    public CompletableFuture<List<String>> lrange(String key, long s, long e) { return cf(async().lrange(key, s, e)); }
    public CompletableFuture<Long> llen(String key) { return cf(async().llen(key)); }

    // -------------------------------------------------------------------------
    // Set
    // -------------------------------------------------------------------------

    public CompletableFuture<Long> sadd(String key, String... members) { return cf(async().sadd(key, members)); }
    public CompletableFuture<Long> srem(String key, String... members) { return cf(async().srem(key, members)); }
    public CompletableFuture<Set<String>> smembers(String key)         { return cf(async().smembers(key)); }
    public CompletableFuture<Boolean> sismember(String key, String m)  { return cf(async().sismember(key, m)); }

    // -------------------------------------------------------------------------
    // Sorted Set
    // -------------------------------------------------------------------------

    public CompletableFuture<Long> zadd(String key, double score, String member) { return cf(async().zadd(key, score, member)); }
    public CompletableFuture<List<String>> zrange(String key, long s, long e)    { return cf(async().zrange(key, s, e)); }
    public CompletableFuture<List<String>> zrevrange(String key, long s, long e) { return cf(async().zrevrange(key, s, e)); }
    public CompletableFuture<Double> zscore(String key, String member)           { return cf(async().zscore(key, member)); }
    public CompletableFuture<Long> zrem(String key, String... members)           { return cf(async().zrem(key, members)); }
    public CompletableFuture<Long> zcard(String key)                             { return cf(async().zcard(key)); }

    // -------------------------------------------------------------------------
    // Pipeline batch
    // -------------------------------------------------------------------------

    /**
     * Execute multiple async commands as a single network batch.
     * Commands are buffered until {@code flushCommands()} is called automatically.
     * Returns a list of futures that can be awaited individually or collectively.
     *
     * <pre>{@code
     * List&lt;CompletableFuture&lt;?&gt;&gt; futures = async.batch(cmds -> List.of(
     *     cmds.set("k1","v1"),
     *     cmds.set("k2","v2"),
     *     cmds.incr("counter")
     * ));
     * CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
     * }</pre>
     */
    public List<CompletableFuture<?>> batch(
        java.util.function.Function<RedisClusterAsyncCommands<String, String>, List<RedisFuture<?>>> commands) {
        // StatefulConnection is the common interface of both
        // StatefulRedisConnection and StatefulRedisClusterConnection.
        // It provides setAutoFlushCommands / flushCommands on both.
        io.lettuce.core.api.StatefulConnection<String, String> conn =
            factory.getConfig().getMode() == LettuceConfig.Mode.CLUSTER
                ? factory.clusterConnection()
                : factory.standaloneConnection();
        conn.setAutoFlushCommands(false);
        try {
            List<RedisFuture<?>> futures = commands.apply(factory.async());
            conn.flushCommands();
            return futures.stream().map(RedisFuture::toCompletableFuture).collect(Collectors.toList());
        } finally {
            conn.setAutoFlushCommands(true);
        }
    }

    /**
     * Convenience: fire a batch and wait for ALL to complete.
     * Returns when the last command finishes (or throws on first failure).
     */
    public void batchSync(
        java.util.function.Function<RedisClusterAsyncCommands<String, String>, List<RedisFuture<?>>> commands) {
        List<CompletableFuture<?>> futures = batch(commands);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    public CompletableFuture<String> ping() { return cf(async().ping()); }
    public CompletableFuture<Long> publish(String channel, String message) { return cf(async().publish(channel, message)); }
}
