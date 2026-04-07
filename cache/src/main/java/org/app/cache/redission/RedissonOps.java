package org.app.cache.redission;

import org.redisson.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Core key-value (Bucket) operations via Redisson.
 * <p>
 * Redisson stores values as typed objects using a codec (default: Jackson JSON).
 * Unlike raw Jedis/Lettuce, you don't serialize manually — just pass your object.
 * Compression (if needed) is handled at the codec level via MsgPack or Kryo.
 *
 * <pre>{@code
 * RedissonOps ops = RedissonOps.of(factory);
 *
 * // Set / get any object — codec handles serialization
 * ops.set("user:1", user);
 * ops.set("user:1", user, Duration.ofMinutes(5));
 * Optional&lt;User&gt; u = ops.get("user:1", User.class);
 *
 * // Atomic compare-and-set
 * boolean swapped = ops.compareAndSet("key", expectedVal, newVal);
 *
 * // Async
 * CompletableFuture&lt;Void&gt; f = ops.setAsync("user:1", user);
 * CompletableFuture&lt;User&gt; g = ops.getAsync("user:1", User.class);
 *
 * // Counters (AtomicLong)
 * long v = ops.incr("counter");
 * long v = ops.incrBy("counter", 5);
 *
 * // Batch get (pipeline under the hood)
 * Map&lt;String, User&gt; results = ops.mget(User.class, "user:1", "user:2");
 * }</pre>
 */
public final class RedissonOps {

    private final RedissonClient client;

    private RedissonOps(RedissonClient client) {
        this.client = client;
    }

    public static RedissonOps of(RedissonFactory factory) {
        return new RedissonOps(factory.client());
    }

    public RedissonClient client() { return client; }

    // -------------------------------------------------------------------------
    // Bucket — typed set / get / delete
    // -------------------------------------------------------------------------

    /** Store any object. Codec handles serialization (Jackson by default). */
    public <T> void set(String key, T value) {
        client.<T>getBucket(key).set(value);
    }

    /** Store with TTL. */
    public <T> void set(String key, T value, Duration ttl) {
        client.<T>getBucket(key).set(value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Set only if key does NOT exist. Returns true if stored. */
    public <T> boolean setNx(String key, T value) {
        return client.<T>getBucket(key).setIfAbsent(value);
    }

    /** Set only if key does NOT exist, with TTL. Returns true if stored. */
    public <T> boolean setNx(String key, T value, Duration ttl) {
        return client.<T>getBucket(key).setIfAbsent(value, ttl);
    }

    /** Set only if key EXISTS. Returns true if stored. */
    public <T> boolean setXx(String key, T value) {
        return client.<T>getBucket(key).setIfExists(value);
    }

    /** Set only if key EXISTS, with TTL. */
    public <T> boolean setXx(String key, T value, Duration ttl) {
        return client.<T>getBucket(key).setIfExists(value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Get and cast to type. Returns empty if key missing or wrong type. */
    public <T> Optional<T> get(String key, Class<T> type) {
        Object val = client.getBucket(key).get();
        if (val == null) return Optional.empty();
        return Optional.of(type.cast(val));
    }

    /** Get without type cast — use when T is already known at call site. */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        T val = (T) client.getBucket(key).get();
        return Optional.ofNullable(val);
    }

    /** Get and delete atomically (GETDEL). */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getDel(String key) {
        T val = (T) client.getBucket(key).getAndDelete();
        return Optional.ofNullable(val);
    }

    /** Get and set TTL atomically (GETEX). */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAndExpire(String key, Duration ttl) {
        T val = (T) client.getBucket(key).getAndExpire(ttl);
        return Optional.ofNullable(val);
    }

    /** Get and set new value atomically. Returns old value. */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAndSet(String key, T newValue) {
        T old = (T) client.getBucket(key).getAndSet(newValue);
        return Optional.ofNullable(old);
    }

    /** Atomic compare-and-set. Returns true if swapped. */
    public <T> boolean compareAndSet(String key, T expected, T update) {
        return client.<T>getBucket(key).compareAndSet(expected, update);
    }

    public boolean exists(String key)  { return client.getBucket(key).isExists(); }
    public boolean delete(String key)  { return client.getBucket(key).delete(); }

    public long delete(String... keys) {
        return client.getKeys().delete(keys);
    }

    public boolean expire(String key, Duration ttl) {
        return client.getBucket(key).expire(ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean expireAt(String key, Instant instant) {
        return client.getBucket(key).expireAt(instant.toEpochMilli());
    }

    public boolean persist(String key) {
        return client.getBucket(key).clearExpire();
    }

    /** Returns remaining TTL, or -1 if no expiry, or -2 if key missing. */
    public long ttlSeconds(String key) {
        long ttl = client.getBucket(key).remainTimeToLive();
        if (ttl < 0) return ttl;
        return ttl / 1_000;
    }

    public long ttlMillis(String key) {
        return client.getBucket(key).remainTimeToLive();
    }

    public String type(String key) {
        return client.getKeys().getType(key).name();
    }

    // -------------------------------------------------------------------------
    // Atomic counters (AtomicLong)
    // -------------------------------------------------------------------------

    public long incr(String key)              { return client.getAtomicLong(key).incrementAndGet(); }
    public long incrBy(String key, long delta) { return client.getAtomicLong(key).addAndGet(delta); }
    public long decr(String key)              { return client.getAtomicLong(key).decrementAndGet(); }
    public long decrBy(String key, long delta) { return client.getAtomicLong(key).addAndGet(-delta); }
    public long getCounter(String key)         { return client.getAtomicLong(key).get(); }
    public void setCounter(String key, long v) { client.getAtomicLong(key).set(v); }

    /** Atomic double accumulator. */
    public double incrByFloat(String key, double delta) { return client.getAtomicDouble(key).addAndGet(delta); }
    public double getDouble(String key)                  { return client.getAtomicDouble(key).get(); }

    // -------------------------------------------------------------------------
    // Async bucket ops
    // -------------------------------------------------------------------------

    public <T> CompletableFuture<Void> setAsync(String key, T value) {
        return client.<T>getBucket(key).setAsync(value).toCompletableFuture();
    }

    public <T> CompletableFuture<Void> setAsync(String key, T value, Duration ttl) {
        return client.<T>getBucket(key)
            .setAsync(value, ttl.toMillis(), TimeUnit.MILLISECONDS)
            .toCompletableFuture();
    }

    public <T> CompletableFuture<Optional<T>> getAsync(String key) {
        return client.<T>getBucket(key).getAsync()
            .toCompletableFuture()
            .thenApply(Optional::ofNullable);
    }

    public CompletableFuture<Boolean> deleteAsync(String key) {
        return client.getBucket(key).deleteAsync().toCompletableFuture();
    }

    public CompletableFuture<Boolean> existsAsync(String key) {
        return client.getBucket(key).isExistsAsync().toCompletableFuture();
    }

    // -------------------------------------------------------------------------
    // Multi-get (batch via RBuckets — single round-trip)
    // -------------------------------------------------------------------------

    /**
     * Fetch multiple keys in a single round-trip using Redisson's {@link RBuckets}.
     * Returns a map of key → value (missing keys are absent from map).
     *
     * <pre>
     * Map&lt;String, User&gt; users = ops.mget(User.class, "user:1", "user:2", "user:3");
     * </pre>
     */
    public <T> Map<String, T> mget(Class<T> type, String... keys) {
        Map<String, Object> raw = client.getBuckets().get(keys);
        Map<String, T> result   = new LinkedHashMap<>();
        raw.forEach((k, v) -> result.put(k, type.cast(v)));
        return result;
    }

    /**
     * Set multiple key-value pairs in a single round-trip.
     */
    public <T> void mset(Map<String, T> entries) {
        client.getBuckets().set(Collections.unmodifiableMap(entries));
    }

    // -------------------------------------------------------------------------
    // Key scan / listing
    // -------------------------------------------------------------------------

    /** Scan keys matching a glob pattern. Uses SCAN internally — safe for prod. */
    public List<String> scanKeys(String pattern) {
        return scanKeys(pattern, 200);
    }

    public List<String> scanKeys(String pattern, int batchSize) {
        List<String> result = new ArrayList<>();
        Iterable<String> keys = client.getKeys().getKeysByPattern(pattern, batchSize);
        keys.forEach(result::add);
        return result;
    }

    public long countKeys(String pattern) {
        return client.getKeys().countExists(scanKeys(pattern).toArray(new String[0]));
    }

    public long dbSize() {
        return client.getKeys().count();
    }

    // -------------------------------------------------------------------------
    // Batch (RBatch — pipeline all commands in one network round-trip)
    // -------------------------------------------------------------------------

    /**
     * Execute multiple commands as a single pipeline batch.
     * <p>
     * {@link RBatch} buffers all commands and flushes in one network call.
     * Results are returned as a list in the same order as commands were added.
     *
     * <pre>
     * List&lt;Object&gt; results = ops.batch(batch -> {
     *     batch.getBucket("k1").setAsync("v1");
     *     batch.getBucket("k2").setAsync("v2");
     *     batch.getAtomicLong("counter").incrementAndGetAsync();
     * });
     * </pre>
     */
    public List<Object> batch(java.util.function.Consumer<RBatch> commands) {
        RBatch batch = client.createBatch(BatchOptions.defaults()
            .executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC));
        commands.accept(batch);
        BatchResult<?> result = batch.execute();
        return new ArrayList<>(result.getResponses());
    }

    /**
     * Async batch — returns CompletableFuture of all results.
     */
    public CompletableFuture<List<Object>> batchAsync(java.util.function.Consumer<RBatch> commands) {
        RBatch batch = client.createBatch(BatchOptions.defaults()
            .executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC));
        commands.accept(batch);
        return batch.executeAsync().toCompletableFuture()
            .thenApply(r -> new ArrayList<>(r.getResponses()));
    }

    // -------------------------------------------------------------------------
    // Scripting (Lua)
    // -------------------------------------------------------------------------

    /**
     * Execute a Lua script.
     *
     * <pre>
     * Long result = ops.eval(
     *     "return redis.call('incr', KEYS[1])",
     *     RScript.ReturnType.INTEGER,
     *     List.of("mykey")
     * );
     * </pre>
     */
    public <T> T eval(String key ,String luaScript, RScript.ReturnType returnType, List<Object> keys, Object... args) {
        return client.getScript().eval(key, RScript.Mode.READ_WRITE, luaScript, returnType, keys, args);
    }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    public static final class RedissonException extends RuntimeException {
        private static final long serialVersionUID = -7062794769145689560L;

        public RedissonException(String msg, Throwable cause) { super(msg, cause); }
        public RedissonException(String msg)                  { super(msg); }
    }
}
