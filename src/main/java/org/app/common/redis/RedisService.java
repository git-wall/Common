package org.app.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Stores a key-value pair in Redis with a default TTL.
     *
     * @param key   The key to store.
     * @param value The value to store.
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Stores a key-value pair in Redis with a custom TTL.
     *
     * @param key      The key to store.
     * @param value    The value to store.
     * @param timeout  The time-to-live for the key.
     * @param timeUnit The unit of time for the TTL.
     */
    public void setWithTimeout(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Retrieves a value from Redis by its key.
     *
     * @param key The key to retrieve.
     * @return The value associated with the key, or null if not found.
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key The key to delete.
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Sets a key-value pair in Redis only if the key does not already exist.
     *
     * @param key   The key to store.
     * @param value The value to store.
     * @return True if the key was set, false if the key already exists.
     */
    public boolean setIfAbsent(String key, Object value) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
        return result != null && result;
    }

    /**
     * Sets a key-value pair in Redis with a TTL only if the key does not already exist.
     *
     * @param key      The key to store.
     * @param value    The value to store.
     * @param timeout  The time-to-live for the key.
     * @param timeUnit The unit of time for the TTL.
     * @return True if the key was set, false if the key already exists.
     */
    public boolean setIfAbsentWithTimeout(String key, Object value, long timeout, TimeUnit timeUnit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
        return result != null && result;
    }

    /**
     * Retrieves a value from Redis by its key or loads it using the provided loader if the key does not exist.
     *
     * @param <R>    The type of the value to be returned.
     * @param key    The key to retrieve from Redis.
     * @param loader A supplier function to load the value if the key does not exist in Redis.
     * @return The value associated with the key if it exists, or the value provided by the loader.
     */
    public <R> R getOrLoad(String key, Supplier<R> loader) {
        if (hasKey(key)) {
            return (R) get(key);
        } else {
            return loader.get();
        }
    }

    // ===================================================================
    // Skip list | Rank, Score, Range
    // ===================================================================

    /**
     * Adds a value to a sorted set with a given score (simulates skip list insert).
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Gets a range of elements by index (simulates skip list range query).
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Gets elements by score range (inclusive, simulates range search).
     */
    public Set<Object> zRangeByScore(String key, double minScore, double maxScore) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
    }

    /**
     * Remove elements by score range (inclusive, simulates range search).
     */
    public void zRemove(String key, Object value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * Gets the score of a member.
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * Gets the rank (index) of an element in the sorted set.
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }
}