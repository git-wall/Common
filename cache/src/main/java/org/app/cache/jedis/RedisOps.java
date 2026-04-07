package org.app.cache.jedis;

import lombok.Getter;
import org.app.cache.Compression;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * High-level Redis operations built on top of {@link RedisClientFactory}.
 * <p>
 * Values are automatically compressed/decompressed with LZ4 when
 * {@code compressionEnabled = true} in the config and the payload exceeds
 * {@code compressionThresholdBytes}.
 *
 * <pre> {@code
 * RedisOps ops = RedisOps.of(factory);
 *
 * // String ops
 * ops.set("key", "value");
 * ops.set("key", "value", 3600);          // with TTL (seconds)
 * Optional<String> v = ops.get("key");
 *
 * // JSON / large objects — compress automatically
 * ops.setBinary("blob", hugeBytes);
 * byte[] back = ops.getBinary("blob");
 *
 * // Hash
 * ops.hset("user:1", "name", "Alice");
 * Map<String,String> user = ops.hgetAll("user:1");
 *
 * // Pipeline batch
 * ops.pipeline(p -> {
 *     p.set("a", "1");
 *     p.set("b", "2");
 * });
 * }</pre>
 */
public final class RedisOps {

    @Getter
    private final RedisClientFactory factory;
    private final boolean compressionEnabled;
    private final int compressionThreshold;

    private RedisOps(RedisClientFactory factory) {
        this.factory = factory;
        this.compressionEnabled = factory.getConfig().isCompressionEnabled();
        this.compressionThreshold = factory.getConfig().getCompressionThresholdBytes();
    }

    public static RedisOps of(RedisClientFactory factory) {
        return new RedisOps(factory);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private <T> T withJedis(Function<Jedis, T> action) {
        try (Jedis jedis = factory.getResource()) {
            return action.apply(jedis);
        }
    }

    private void withJedisVoid(Consumer<Jedis> action) {
        try (Jedis jedis = factory.getResource()) {
            action.accept(jedis);
        }
    }

    private byte[] toStoredBytes(byte[] raw) {
        if (!compressionEnabled || raw == null || raw.length < compressionThreshold) return raw;
        return Compression.compress(raw);
    }

    private byte[] fromStoredBytes(byte[] stored) {
        if (!compressionEnabled || stored == null || stored.length == 0) return stored;
        // Heuristic: compressed data starts with 4-byte int header written by Compression class.
        // We attempt to decompress; if it fails we return raw (backward-compat with uncompressed keys).
        try {
            return Compression.decompress(stored);
        } catch (Exception e) {
            return stored;
        }
    }

    private byte[] keyBytes(String key) {
        return key.getBytes(StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // String operations
    // -------------------------------------------------------------------------

    public void set(String key, String value) {
        withJedisVoid(j ->
            j.set(
            keyBytes(key),
            toStoredBytes(value.getBytes(StandardCharsets.UTF_8))
        ));
    }

    /** Set with TTL in seconds. */
    public void set(String key, String value, long ttlSeconds) {
        withJedisVoid(j -> j.setex(
            keyBytes(key),
            ttlSeconds,
            toStoredBytes(value.getBytes(StandardCharsets.UTF_8))
        ));
    }

    /** Set only if key does NOT exist. Returns true if set succeeded. */
    public boolean setNx(String key, String value) {
        return withJedis(j -> j.setnx(
            keyBytes(key),
            toStoredBytes(value.getBytes(StandardCharsets.UTF_8))
        ) == 1L);
    }

    /** Set only if key does NOT exist, with TTL. Returns true if set succeeded. */
    public boolean setNx(String key, String value, long ttlSeconds) {
        return withJedis(j -> {
            byte[] k = keyBytes(key);
            byte[] v = toStoredBytes(value.getBytes(StandardCharsets.UTF_8));
            // SET key value NX EX ttl
            redis.clients.jedis.params.SetParams params =
                redis.clients.jedis.params.SetParams.setParams().nx().ex(ttlSeconds);
            return "OK".equals(j.set(k, v, params));
        });
    }

    public Optional<String> get(String key) {
        byte[] raw = withJedis(j -> j.get(keyBytes(key)));
        if (raw == null) return Optional.empty();
        return Optional.of(new String(fromStoredBytes(raw), StandardCharsets.UTF_8));
    }

    public boolean exists(String key) {
        return withJedis(j -> j.exists(keyBytes(key)));
    }

    public long del(String... keys) {
        byte[][] byteKeys = Arrays.stream(keys).map(this::keyBytes).toArray(byte[][]::new);
        return withJedis(j -> j.del(byteKeys));
    }

    public boolean expire(String key, long ttlSeconds) {
        return withJedis(j -> j.expire(keyBytes(key), ttlSeconds) == 1L);
    }

    public long ttl(String key) {
        return withJedis(j -> j.ttl(keyBytes(key)));
    }

    public long incr(String key) {
        return withJedis(j -> j.incr(key));
    }

    public long incrBy(String key, long delta) {
        return withJedis(j -> j.incrBy(key, delta));
    }

    // -------------------------------------------------------------------------
    // Binary operations (raw bytes, always compressed if threshold met)
    // -------------------------------------------------------------------------

    public void setBinary(String key, byte[] data) {
        withJedisVoid(j -> j.set(keyBytes(key), toStoredBytes(data)));
    }

    public void setBinary(String key, byte[] data, long ttlSeconds) {
        withJedisVoid(j -> j.setex(keyBytes(key), ttlSeconds, toStoredBytes(data)));
    }

    public byte[] getBinary(String key) {
        byte[] stored = withJedis(j -> j.get(keyBytes(key)));
        return fromStoredBytes(stored);
    }

    // -------------------------------------------------------------------------
    // Hash operations
    // -------------------------------------------------------------------------

    public void hset(String key, String field, String value) {
        withJedisVoid(j -> j.hset(key, field, value));
    }

    public void hsetAll(String key, Map<String, String> fields) {
        withJedisVoid(j -> j.hset(key, fields));
    }

    public Optional<String> hget(String key, String field) {
        return Optional.ofNullable(withJedis(j -> j.hget(key, field)));
    }

    public Map<String, String> hgetAll(String key) {
        return withJedis(j -> j.hgetAll(key));
    }

    public long hdel(String key, String... fields) {
        return withJedis(j -> j.hdel(key, fields));
    }

    public boolean hexists(String key, String field) {
        return withJedis(j -> j.hexists(key, field));
    }

    // -------------------------------------------------------------------------
    // List operations
    // -------------------------------------------------------------------------

    /** Push to HEAD (left). */
    public long lpush(String key, String... values) {
        return withJedis(j -> j.lpush(key, values));
    }

    /** Push to TAIL (right). */
    public long rpush(String key, String... values) {
        return withJedis(j -> j.rpush(key, values));
    }

    public Optional<String> lpop(String key) {
        return Optional.ofNullable(withJedis(j -> j.lpop(key)));
    }

    public Optional<String> rpop(String key) {
        return Optional.ofNullable(withJedis(j -> j.rpop(key)));
    }

    public List<String> lrange(String key, long start, long stop) {
        return withJedis(j -> j.lrange(key, start, stop));
    }

    public long llen(String key) {
        return withJedis(j -> j.llen(key));
    }

    // -------------------------------------------------------------------------
    // Set operations
    // -------------------------------------------------------------------------

    public long sadd(String key, String... members) {
        return withJedis(j -> j.sadd(key, members));
    }

    public long srem(String key, String... members) {
        return withJedis(j -> j.srem(key, members));
    }

    public Set<String> smembers(String key) {
        return withJedis(j -> j.smembers(key));
    }

    public boolean sismember(String key, String member) {
        return withJedis(j -> j.sismember(key, member));
    }

    // -------------------------------------------------------------------------
    // Sorted Set (ZSet)
    // -------------------------------------------------------------------------

    public long zadd(String key, double score, String member) {
        return withJedis(j -> j.zadd(key, score, member));
    }

    public List<String> zrange(String key, long start, long stop) {
        return withJedis(j -> j.zrange(key, start, stop));
    }

    public List<String> zrevrange(String key, long start, long stop) {
        return withJedis(j -> j.zrevrange(key, start, stop));
    }

    public Double zscore(String key, String member) {
        return withJedis(j -> j.zscore(key, member));
    }

    public long zrem(String key, String... members) {
        return withJedis(j -> j.zrem(key, members));
    }

    // -------------------------------------------------------------------------
    // HyperLogLog
    // -------------------------------------------------------------------------

    /**
     * Add the specified elements to the HyperLogLog stored at key.
     * Returns 1 if at least one internal register was altered, 0 otherwise.
     * This is a thin wrapper around Jedis#pfadd(String, String...).
     *
     * @param key the HLL key
     * @param elements elements to add
     * @return 1 if at least one register changed, 0 if no change or no elements
     */
    public long pfadd(String key, String... elements) {
        if (elements == null || elements.length == 0) return 0L;
        return withJedis(j -> j.pfadd(key, elements));
    }

    /**
     * Add the specified binary elements to the HyperLogLog stored at key.
     * Uses binary API (byte[]). Useful when you want to control encoding or avoid
     * extra allocations for already-serialized values.
     *
     * @param key the HLL key
     * @param elements binary elements to add
     * @return 1 if at least one register changed, 0 if no change or no elements
     */
    public long pfaddBytes(String key, byte[]... elements) {
        if (elements == null || elements.length == 0) return 0L;
        return withJedis(j -> j.pfadd(keyBytes(key), elements));
    }

    /**
     * Return the approximated cardinality of the set(s) observed by the HyperLogLog.
     * This wraps Jedis#pfcount(String...). If multiple keys are provided, the
     * cardinality of the union is returned.
     *
     * @param keys one or more HLL keys
     * @return approximated cardinality (0 if no keys provided)
     */
    public long pfcount(String... keys) {
        if (keys == null || keys.length == 0) return 0L;
        return withJedis(j -> j.pfcount(keys));
    }

    /**
     * Convenience overload that accepts a Collection of keys.
     *
     * @param keys collection of HLL keys
     * @return approximated cardinality (0 if collection is null/empty)
     */
    public long pfcount(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return 0L;
        return withJedis(j -> j.pfcount(keys.toArray(new String[0])));
    }

    /**
     * Merge multiple HyperLogLogs into a single destination key (in-place).
     * Thin wrapper around Jedis#pfmerge(String, String...). No-op when no source keys are provided.
     *
     * @param destKey destination HLL key
     * @param sourceKeys source HLL keys to merge
     */
    public void pfmerge(String destKey, String... sourceKeys) {
        if (sourceKeys == null || sourceKeys.length == 0) return;
        withJedisVoid(j -> j.pfmerge(destKey, sourceKeys));
    }

    /**
     * Binary version of pfmerge: merge binary keys into destination (byte[] API).
     * Useful when your keys are binary or you want to avoid extra charset conversions.
     *
     * @param destKey destination HLL key
     * @param sourceKeys binary source keys
     */
    public void pfmergeBytes(String destKey, byte[]... sourceKeys) {
        if (sourceKeys == null || sourceKeys.length == 0) return;
        withJedisVoid(j -> j.pfmerge(keyBytes(destKey), sourceKeys));
    }

    // -------------------------------------------------------------------------
    // Pipeline (batch writes — fire and forget results)
    // -------------------------------------------------------------------------

    /**
     * Execute multiple commands in a single pipeline.
     * <pre>
     * ops.pipeline(p -> {
     *     p.set("k1", "v1");
     *     p.expire("k1", 60);
     * });
     * </pre>
     */
    public void pipeline(Consumer<Pipeline> commands) {
        withJedisVoid(j -> {
            Pipeline p = j.pipelined();
            commands.accept(p);
            p.sync();
        });
    }

    // -------------------------------------------------------------------------
    // Scan (safe key iteration — no KEYS *)
    // -------------------------------------------------------------------------

    /**
     * Scan all keys matching pattern and collect into a list.
     * Uses SCAN cursor internally — safe for production.
     */
    public List<String> scanKeys(String pattern) {
        return withJedis(j -> {
            List<String> result = new ArrayList<>();
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().match(pattern).count(200);
            do {
                ScanResult<String> scanResult = j.scan(cursor, params);
                result.addAll(scanResult.getResult());
                cursor = scanResult.getCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
            return result;
        });
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    public String ping() {
        return withJedis(Jedis::ping);
    }

    public void flushDb() {
        withJedisVoid(Jedis::flushDB);
    }

    public String info() {
        return withJedis(Jedis::info);
    }
}
