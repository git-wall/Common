package org.app.cache.lettuce;

import io.lettuce.core.*;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.app.cache.Compression;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Synchronous Lettuce Redis operations with transparent LZ4 compression.
 * <p>
 * One shared Lettuce connection is thread-safe — no borrowing/returning needed.
 * All compressed ops go through the byte-array connection internally.
 *
 * <pre>{@code
 * LettuceOps ops = LettuceOps.of(factory);
 *
 * // String
 * ops.set("key", "value");
 * ops.set("key", "value", 3600);
 * Optional&lt;String&gt; v = ops.get("key");
 *
 * // Binary (auto LZ4)
 * ops.setBinary("blob", bigBytes);
 * byte[] back = ops.getBinary("blob");
 *
 * // Hash
 * ops.hset("user:1", "name", "Alice");
 * Map&lt;String,String&gt; user = ops.hgetAll("user:1");
 *
 * // Pipelined batch
 * ops.pipeline(cmds -> {
 *     cmds.set("a", "1");
 *     cmds.set("b", "2");
 * });
 * }</pre>
 */
public final class LettuceOps {

    private final LettuceClientFactory factory;
    private final boolean              compressionEnabled;
    private final int                  compressionThreshold;

    private LettuceOps(LettuceClientFactory factory) {
        this.factory              = factory;
        this.compressionEnabled   = factory.getConfig().isCompressionEnabled();
        this.compressionThreshold = factory.getConfig().getCompressionThresholdBytes();
    }

    public static LettuceOps of(LettuceClientFactory factory) {
        return new LettuceOps(factory);
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private RedisClusterCommands<String, String> sync() {
        return factory.sync();
    }

    private RedisClusterCommands<byte[], byte[]> binarySync() {
        return factory.binarySync();
    }

    LettuceClientFactory factory() { return factory; }

    private byte[] toStoredBytes(byte[] raw) {
        if (!compressionEnabled || raw == null || raw.length < compressionThreshold) return raw;
        return Compression.compress(raw);
    }

    private byte[] fromStoredBytes(byte[] stored) {
        if (!compressionEnabled || stored == null) return stored;
        return Compression.decompress(stored);
    }

    private byte[] kb(String key) { return key.getBytes(StandardCharsets.UTF_8); }
    private byte[] vb(String val) { return val.getBytes(StandardCharsets.UTF_8); }
    private String vs(byte[] b)   { return b == null ? null : new String(b, StandardCharsets.UTF_8); }

    // -------------------------------------------------------------------------
    // String operations (with transparent compression)
    // -------------------------------------------------------------------------

    public void set(String key, String value) {
        binarySync().set(kb(key), toStoredBytes(vb(value)));
    }

    public void set(String key, String value, long ttlSeconds) {
        binarySync().setex(kb(key), ttlSeconds, toStoredBytes(vb(value)));
    }

    /** SET with options: NX, XX, GET, EX, PX, EXAT, PXAT, KEEPTTL */
    public void set(String key, String value, SetArgs args) {
        binarySync().set(kb(key), toStoredBytes(vb(value)), toSetArgs(args));
    }

    /** SET NX (only if not exists). Returns true if set. */
    public boolean setNx(String key, String value) {
        return binarySync().setnx(kb(key), toStoredBytes(vb(value)));
    }

    /** SET NX with TTL — atomic. Returns true if set. */
    public boolean setNx(String key, String value, long ttlSeconds) {
        String result = binarySync().set(kb(key), toStoredBytes(vb(value)),
            io.lettuce.core.SetArgs.Builder.nx().ex(ttlSeconds));
        return "OK".equals(result);
    }

    /** SET XX (only if exists). Returns true if set. */
    public boolean setXx(String key, String value, long ttlSeconds) {
        String result = binarySync().set(kb(key), toStoredBytes(vb(value)),
            io.lettuce.core.SetArgs.Builder.xx().ex(ttlSeconds));
        return "OK".equals(result);
    }

    public Optional<String> get(String key) {
        byte[] raw = binarySync().get(kb(key));
        return Optional.ofNullable(vs(fromStoredBytes(raw)));
    }

    /** GETDEL — get and delete atomically (Redis 6.2+) */
    public Optional<String> getDel(String key) {
        byte[] raw = binarySync().getdel(kb(key));
        return Optional.ofNullable(vs(fromStoredBytes(raw)));
    }

    /** GETEX — get and set TTL atomically (Redis 6.2+) */
    public Optional<String> getEx(String key, long ttlSeconds) {
        byte[] raw = binarySync().getex(kb(key),
            io.lettuce.core.GetExArgs.Builder.ex(ttlSeconds));
        return Optional.ofNullable(vs(fromStoredBytes(raw)));
    }

    public boolean exists(String key) {
        return binarySync().exists(kb(key)) > 0;
    }

    public long del(String... keys) {
        byte[][] bkeys = Arrays.stream(keys).map(this::kb).toArray(byte[][]::new);
        return binarySync().del(bkeys);
    }

    public boolean expire(String key, long ttlSeconds) {
        return Boolean.TRUE.equals(binarySync().expire(kb(key), ttlSeconds));
    }

    public boolean expireAt(String key, long unixTimestampSeconds) {
        return Boolean.TRUE.equals(binarySync().expireat(kb(key), unixTimestampSeconds));
    }

    public long ttl(String key) {
        Long v = binarySync().ttl(kb(key));
        return v == null ? -2L : v;
    }

    public boolean persist(String key) {
        return Boolean.TRUE.equals(binarySync().persist(kb(key)));
    }

    public long incr(String key)              { return sync().incr(key); }
    public long incrBy(String key, long d)    { return sync().incrby(key, d); }
    public double incrByFloat(String key, double d) { return sync().incrbyfloat(key, d); }
    public long decr(String key)              { return sync().decr(key); }
    public long decrBy(String key, long d)    { return sync().decrby(key, d); }

    public String type(String key) { return sync().type(key); }

    /** OBJECT ENCODING key — useful for debugging memory usage */
    public String encoding(String key) { return sync().objectEncoding(key); }

    // -------------------------------------------------------------------------
    // Binary operations (raw bytes, always LZ4 if threshold met)
    // -------------------------------------------------------------------------

    public void setBinary(String key, byte[] data) {
        binarySync().set(kb(key), toStoredBytes(data));
    }

    public void setBinary(String key, byte[] data, long ttlSeconds) {
        binarySync().setex(kb(key), ttlSeconds, toStoredBytes(data));
    }

    public byte[] getBinary(String key) {
        return fromStoredBytes(binarySync().get(kb(key)));
    }

    // -------------------------------------------------------------------------
    // Hash operations
    // -------------------------------------------------------------------------

    public void hset(String key, String field, String value) {
        sync().hset(key, field, value);
    }

    public void hsetAll(String key, Map<String, String> fields) {
        sync().hset(key, fields);
    }

    /** HSETNX — only set if field doesn't exist */
    public boolean hsetNx(String key, String field, String value) {
        return Boolean.TRUE.equals(sync().hsetnx(key, field, value));
    }

    public Optional<String> hget(String key, String field) {
        return Optional.ofNullable(sync().hget(key, field));
    }

    public Map<String, String> hgetAll(String key) {
        return sync().hgetall(key);
    }

    public List<String> hmget(String key, String... fields) {
        return sync().hmget(key, fields).stream()
            .map(kv -> kv.hasValue() ? kv.getValue() : null)
            .collect(Collectors.toList());
    }

    public long hdel(String key, String... fields) {
        return sync().hdel(key, fields);
    }

    public boolean hexists(String key, String field) {
        return Boolean.TRUE.equals(sync().hexists(key, field));
    }

    public long hlen(String key)                          { return sync().hlen(key); }
    public List<String> hkeys(String key)                  { return sync().hkeys(key); }
    public List<String> hvals(String key)                 { return sync().hvals(key); }
    public long hincrBy(String key, String f, long d)     { return sync().hincrby(key, f, d); }
    public double hincrByFloat(String key, String f, double d) { return sync().hincrbyfloat(key, f, d); }

    // -------------------------------------------------------------------------
    // List operations
    // -------------------------------------------------------------------------

    public long lpush(String key, String... values)  { return sync().lpush(key, values); }
    public long rpush(String key, String... values)  { return sync().rpush(key, values); }
    public long lpushX(String key, String... values) { return sync().lpushx(key, values); }
    public long rpushX(String key, String... values) { return sync().rpushx(key, values); }

    public Optional<String> lpop(String key)  { return Optional.ofNullable(sync().lpop(key)); }
    public Optional<String> rpop(String key)  { return Optional.ofNullable(sync().rpop(key)); }

    /** BLPOP — blocking pop, returns (key, value) pair or empty on timeout */
    public Optional<KeyValue<String, String>> blpop(long timeoutSeconds, String... keys) {
        return Optional.ofNullable(sync().blpop(timeoutSeconds, keys));
    }

    public Optional<String> lindex(String key, long index) { return Optional.ofNullable(sync().lindex(key, index)); }
    public List<String> lrange(String key, long start, long stop) { return sync().lrange(key, start, stop); }
    public long llen(String key) { return sync().llen(key); }
    public void ltrim(String key, long start, long stop) { sync().ltrim(key, start, stop); }
    public long linsertBefore(String key, String pivot, String value) { return sync().linsert(key, false, pivot, value); }
    public long linsertAfter(String key, String pivot, String value)  { return sync().linsert(key, true, pivot, value); }
    public long lrem(String key, long count, String value) { return sync().lrem(key, count, value); }

    // -------------------------------------------------------------------------
    // Set operations
    // -------------------------------------------------------------------------

    public long sadd(String key, String... members) { return sync().sadd(key, members); }
    public long srem(String key, String... members) { return sync().srem(key, members); }
    public Set<String> smembers(String key)         { return sync().smembers(key); }
    public boolean sismember(String key, String m)  { return Boolean.TRUE.equals(sync().sismember(key, m)); }
    public long scard(String key)                   { return sync().scard(key); }
    public Optional<String> spop(String key)        { return Optional.ofNullable(sync().spop(key)); }
    public Set<String> spop(String key, long count) { return sync().spop(key, count); }
    public Set<String> sinter(String... keys)       { return sync().sinter(keys); }
    public Set<String> sunion(String... keys)       { return sync().sunion(keys); }
    public Set<String> sdiff(String... keys)        { return sync().sdiff(keys); }

    /** SMISMEMBER — bulk membership check (Redis 6.2+) */
    public List<Boolean> smismember(String key, String... members) {
        return sync().smismember(key, members);
    }

    // -------------------------------------------------------------------------
    // Sorted Set (ZSet) operations
    // -------------------------------------------------------------------------

    public long zadd(String key, double score, String member) { return sync().zadd(key, score, member); }
    public long zadd(String key, ScoredValue<String>... scoredValues) { return sync().zadd(key, scoredValues); }

    /** ZADD NX — only add new members */
    public long zaddNx(String key, double score, String member) {
        return sync().zadd(key, ZAddArgs.Builder.nx(), score, member);
    }

    /** ZADD XX — only update existing members */
    public long zaddXx(String key, double score, String member) {
        return sync().zadd(key, ZAddArgs.Builder.xx(), score, member);
    }

    public List<String> zrange(String key, long start, long stop)        { return sync().zrange(key, start, stop); }
    public List<String> zrevrange(String key, long start, long stop)      { return sync().zrevrange(key, start, stop); }
    public List<ScoredValue<String>> zrangeWithScores(String key, long s, long e) { return sync().zrangeWithScores(key, s, e); }
    public List<String> zrangeByScore(String key, double min, double max) { return sync().zrangebyscore(key, Range.create(min, max)); }
    public List<String> zrevrangeByScore(String key, double max, double min) { return sync().zrevrangebyscore(key, Range.create(min, max)); }

    public Double zscore(String key, String member)  { return sync().zscore(key, member); }
    public Long zrank(String key, String member)     { return sync().zrank(key, member); }
    public Long zrevrank(String key, String member)  { return sync().zrevrank(key, member); }
    public long zrem(String key, String... members)  { return sync().zrem(key, members); }
    public long zcard(String key)                    { return sync().zcard(key); }
    public long zcount(String key, double min, double max) { return sync().zcount(key, Range.create(min, max)); }
    public Double zincrby(String key, double incr, String member) { return sync().zincrby(key, incr, member); }
    public long zremrangeByRank(String key, long start, long stop) { return sync().zremrangebyrank(key, start, stop); }
    public long zremrangeByScore(String key, double min, double max) { return sync().zremrangebyscore(key, Range.create(min, max)); }

    // -------------------------------------------------------------------------
    // Geo operations (Redis 3.2+)
    // -------------------------------------------------------------------------

    public long geoadd(String key, double lon, double lat, String member) {
        return sync().geoadd(key, lon, lat, member);
    }

    public Double geodist(String key, String from, String to, GeoArgs.Unit unit) {
        return sync().geodist(key, from, to, unit);
    }

    // -------------------------------------------------------------------------
    // Scan (cursor — safe for production)
    // -------------------------------------------------------------------------

    public List<String> scanKeys(String pattern) {
        return scanKeys(pattern, 200);
    }

    public List<String> scanKeys(String pattern, int count) {
        List<String> result = new ArrayList<>();
        ScanCursor cursor   = ScanCursor.INITIAL;
        ScanArgs   args     = ScanArgs.Builder.matches(pattern).limit(count);
        do {
            KeyScanCursor<String> scanResult = sync().scan(cursor, args);
            result.addAll(scanResult.getKeys());
            cursor = scanResult;
        } while (!cursor.isFinished());
        return result;
    }

    public long countKeys(String pattern) {
        return scanKeys(pattern).size();
    }

    // -------------------------------------------------------------------------
    // Pipelined batch (fire-and-forget results)
    // -------------------------------------------------------------------------

    /**
     * Execute multiple commands in a pipeline.
     * Commands are buffered and flushed in one network round-trip.
     * <p>
     * In CLUSTER mode, commands for keys in different slots are still batched
     * at the transport level via {@code setAutoFlushCommands}.
     *
     * <pre>
     * ops.pipeline(cmds -> {
     *     cmds.set("k1", "v1");
     *     cmds.set("k2", "v2");
     *     cmds.expire("k1", 60);
     * });
     * </pre>
     */
    public void pipeline(Consumer<RedisClusterCommands<String, String>> commands) {
        // Both StatefulRedisConnection and StatefulRedisClusterConnection implement
        // StatefulConnection which provides setAutoFlushCommands / flushCommands.
        io.lettuce.core.api.StatefulConnection<String, String> conn =
            factory.getConfig().getMode() == LettuceConfig.Mode.CLUSTER
                ? factory.clusterConnection()
                : factory.standaloneConnection();
        conn.setAutoFlushCommands(false);
        try {
            commands.accept(factory.sync());
            conn.flushCommands();
        } finally {
            conn.setAutoFlushCommands(true);
        }
    }

    // -------------------------------------------------------------------------
    // Pub/Sub (fire-and-forget publish)
    // -------------------------------------------------------------------------

    public long publish(String channel, String message) {
        return sync().publish(channel, message);
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    public String ping() { return sync().ping(); }
    public void flushDb() { sync().flushdb(); }
    public String info()  { return sync().info(); }
    public long dbSize()  { return sync().dbsize(); }

    /** Rename a key atomically */
    public void rename(String from, String to) { sync().rename(from, to); }

    /** COPY key (Redis 6.2+) */
    public boolean copy(String source, String dest) {
        return Boolean.TRUE.equals(sync().copy(source, dest));
    }

    // -------------------------------------------------------------------------
    // Internal SetArgs adapter
    // -------------------------------------------------------------------------

    private io.lettuce.core.SetArgs toSetArgs(SetArgs args) { return args; }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    public static final class LettuceException extends RuntimeException {
        private static final long serialVersionUID = 6847266797293410802L;

        public LettuceException(String msg, Throwable cause) { super(msg, cause); }
        public LettuceException(String msg)                  { super(msg); }
    }
}
