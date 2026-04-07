package org.app.cache.jedis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Optional;

/**
 * Serialize/deserialize Java objects to/from Redis using Jackson JSON + LZ4.
 * <p>
 * The JSON bytes are passed to {@link RedisOps#setBinary} which applies LZ4
 * compression when the payload exceeds the configured threshold.
 *
 * <pre>{@code
 * RedisJson rj = RedisJson.of(ops);
 *
 * rj.set("user:1", myUser);
 * rj.set("user:1", myUser, 3600);
 * Optional<User> u = rj.get("user:1", User.class);
 * Optional<List<Order>> orders = rj.getList("orders:1", Order.class);
 * }</pre>
 */
public final class RedisJson {

    private final RedisOps ops;
    private final ObjectMapper mapper;

    private RedisJson(RedisOps ops, ObjectMapper mapper) {
        this.ops = ops;
        this.mapper = mapper;
    }

    /** Create with a default Jackson mapper (JavaTime module registered). */
    public static RedisJson of(RedisOps ops) {
        ObjectMapper m = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new RedisJson(ops, m);
    }

    /** Create with a custom mapper (useful when you have shared config). */
    public static RedisJson of(RedisOps ops, ObjectMapper mapper) {
        return new RedisJson(ops, mapper);
    }

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

    /** Deserialize a JSON array stored as a Redis value. */
    public <T> Optional<List<T>> getList(String key, Class<T> elementType) {
        byte[] raw = ops.getBinary(key);
        if (raw == null) return Optional.empty();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        try {
            return Optional.of(mapper.readValue(raw, listType));
        } catch (Exception e) {
            throw new RedisSerializationException("Failed to deserialize list from key: " + key, e);
        }
    }

    public void delete(String key) {
        ops.del(key);
    }

    public boolean exists(String key) {
        return ops.exists(key);
    }

    // -------------------------------------------------------------------------

    private <T> byte[] serialize(T value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new RedisSerializationException("Failed to serialize object", e);
        }
    }

    private <T> T deserialize(byte[] bytes, Class<T> type) {
        try {
            return mapper.readValue(bytes, type);
        } catch (Exception e) {
            throw new RedisSerializationException("Failed to deserialize to " + type.getSimpleName(), e);
        }
    }

    // -------------------------------------------------------------------------

    public static final class RedisSerializationException extends RuntimeException {
        private static final long serialVersionUID = -8975568697509564071L;

        public RedisSerializationException(String msg, Throwable cause) { super(msg, cause); }
    }
}
