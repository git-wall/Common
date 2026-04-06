package org.app.cache.lettuce;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Serialize/deserialize Java objects to/from Redis using Jackson JSON + LZ4 compression.
 * Backed by the binary Lettuce connection for byte-level control.
 *
 * <pre>{@code
 * LettuceJson json = LettuceJson.of(ops);
 *
 * // Sync
 * json.set("user:1", user);
 * json.set("user:1", user, 3600);
 * Optional&lt;User&gt; u = json.get("user:1", User.class);
 * Optional&lt;List&lt;Order&gt;&gt; orders = json.getList("orders:u1", Order.class);
 *
 * // Async
 * CompletableFuture&lt;Optional&lt;User&gt;&gt; f = json.getAsync("user:1", User.class);
 * }</pre>
 */
public final class LettuceJson {

    private final LettuceOps ops;
    private final LettuceAsyncOps asyncOps;
    private final ObjectMapper    mapper;

    private LettuceJson(LettuceOps ops, LettuceAsyncOps asyncOps, ObjectMapper mapper) {
        this.ops      = ops;
        this.asyncOps = asyncOps;
        this.mapper   = mapper;
    }

    public static LettuceJson of(LettuceOps ops) {
        LettuceAsyncOps asyncOps = LettuceAsyncOps.of(ops.factory());
        ObjectMapper m = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new LettuceJson(ops, asyncOps, m);
    }

    public static LettuceJson of(LettuceOps ops, ObjectMapper customMapper) {
        return new LettuceJson(ops, LettuceAsyncOps.of(ops.factory()), customMapper);
    }

    // -------------------------------------------------------------------------
    // Sync
    // -------------------------------------------------------------------------

    public <T> void set(String key, T value) {
        ops.setBinary(key, serialize(value));
    }

    public <T> void set(String key, T value, long ttlSeconds) {
        ops.setBinary(key, serialize(value), ttlSeconds);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        byte[] raw = ops.getBinary(key);
        if (raw == null) return Optional.empty();
        return Optional.of(deserialize(raw, type));
    }

    public <T> Optional<List<T>> getList(String key, Class<T> elementType) {
        byte[] raw = ops.getBinary(key);
        if (raw == null) return Optional.empty();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        try {
            return Optional.of(mapper.readValue(raw, listType));
        } catch (Exception e) {
            throw new SerializationException("Deserialize list failed: " + key, e);
        }
    }

    public void delete(String key)     { ops.del(key); }
    public boolean exists(String key)  { return ops.exists(key); }

    // -------------------------------------------------------------------------
    // Async
    // -------------------------------------------------------------------------

    public <T> CompletableFuture<Void> setAsync(String key, T value) {
        return asyncOps.setBinary(key, serialize(value)).thenApply(r -> null);
    }

    public <T> CompletableFuture<Void> setAsync(String key, T value, long ttlSeconds) {
        return asyncOps.setBinary(key, serialize(value), ttlSeconds).thenApply(r -> null);
    }

    public <T> CompletableFuture<Optional<T>> getAsync(String key, Class<T> type) {
        return asyncOps.getBinary(key)
            .thenApply(raw -> raw == null ? Optional.empty() : Optional.of(deserialize(raw, type)));
    }

    // -------------------------------------------------------------------------

    private <T> byte[] serialize(T value) {
        try { return mapper.writeValueAsBytes(value); }
        catch (Exception e) { throw new SerializationException("Serialize failed", e); }
    }

    private <T> T deserialize(byte[] bytes, Class<T> type) {
        try { return mapper.readValue(bytes, type); }
        catch (Exception e) { throw new SerializationException("Deserialize to " + type.getSimpleName() + " failed", e); }
    }

    public static final class SerializationException extends RuntimeException {
        private static final long serialVersionUID = -8543463240011162175L;

        public SerializationException(String msg, Throwable cause) { super(msg, cause); }
    }
}
