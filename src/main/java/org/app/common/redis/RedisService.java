package org.app.common.redis;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
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

    public void setWithTimeoutMinute(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }

    public void setWithTimeoutDay(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.DAYS);
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

    public <T, K> void setGroupByIfAbsentWithTimeout(List<T> list,
                                                     Function<T, K> classifier,
                                                     String keyExtractor,
                                                     long timeout) {
        var map = list.stream().collect(Collectors.groupingBy(classifier));
        setIfAbsentWithTimeout(map, keyExtractor, timeout);
    }

    public <T, V> void setIfAbsentWithTimeout(Map<T, V> map, String keyFormat, long timeout) {
        map.forEach((k, v) -> {
            String key = String.format(keyFormat, k);
            setIfAbsentWithTimeout(key, v, timeout, TimeUnit.MILLISECONDS);
        });
    }

    public <T> void setIfAbsentWithTimeout(List<T> list,
                                           String keyFormat,
                                           Function<T, ?> keyExtractor,
                                           long timeout,
                                           TimeUnit timeUnit) {
        list.forEach(e -> {
            String key = String.format(keyFormat, keyExtractor.apply(e));
            setIfAbsentWithTimeout(key, e, timeout, timeUnit);
        });
    }

    public <T> void setIfAbsentWithTimeout(Map<String, T> map,
                                           long timeout,
                                           TimeUnit timeUnit) {
        map.forEach((k, v) -> setIfAbsentWithTimeout(k, v, timeout, timeUnit));
    }

    /**
     * Retrieves a value from Redis by its key or loads it using the provided loader if the key does not exist.
     *
     * @param <R>    The type of the value to be returned.
     * @param key    The key to retrieve from Redis.
     * @param loader A supplier function to load the value if the key does not exist in Redis.
     * @return The value associated with the key if it exists, or the value provided by the loader.
     */
    @SuppressWarnings("unchecked")
    public <R> R getOrLoad(String key, Supplier<R> loader) {
        if (hasKey(key)) {
            var r = get(key);
            if (r != null) {
                return (R) r;
            }
        }
        return loader.get();
    }

    /**
     * Retrieves a value from Redis by its key or loads it using the provided loader if the key does not exist.
     * If the value is loaded, it is also saved in the Redis cache.
     *
     * @param <R>    The type of the value to be returned.
     * @param key    The key to retrieve from Redis.
     * @param loader A supplier function to load the value if the key does not exist in Redis.
     * @return The value associated with the key if it exists, or the value provided by the loader.
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <R> R getOrLoadAndSaveInCache(String key, Supplier<R> loader) {
        if (hasKey(key)) {
            var r = get(key);
            if (r != null) {
                return (R) r;
            }
        }
        R r = loader.get();
        set(key, r);
        return r;
    }

    /**
     * Retrieves a value from Redis by its key or loads it using the provided callable if the key does not exist.
     * If the value is loaded, it is also saved in the Redis cache.
     *
     * @param <R>      The type of the value to be returned.
     * @param key      The key to retrieve from Redis.
     * @param callable A callable function to load the value if the key does not exist in Redis.
     * @return The value associated with the key if it exists, or the value provided by the callable.
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <R> R getOrLoadAndSaveInCache(String key, Callable<R> callable) {
        if (hasKey(key)) {
            var r = get(key);
            if (r != null) {
                return (R) r;
            }
        }
        R r = callable.call();
        set(key, r);
        return r;
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
     * Gets a range of elements by index (simulates a skip list range query).
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

    // ===================================================================
    // Publisher
    // ===================================================================

    public void publish(String pattern, String id, String message) {
        String channel = String.format(pattern, id);
        redisTemplate.convertAndSend(channel, message);
    }

    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}