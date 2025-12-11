package org.app.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOps;

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
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit));
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

    public <T> void setIfAbsentWithTimeout(Map<String, T> map, long timeout, TimeUnit timeUnit) {
        map.forEach((k, v) -> setIfAbsentWithTimeout(k, v, timeout, timeUnit));
    }

    @SneakyThrows
    public <T> T getAside(String key, Supplier<T> loader, int ttl, TimeUnit timeUnit) {
        var data = get(key);
        if (data != null)
            return (T) data;

        T loaded = loader.get();
        setWithTimeout(key, loaded, ttl, timeUnit);
        return loaded;
    }

    @SneakyThrows
    public <T> T getAsideMillisecond(String key, Supplier<T> loader, int ttl) {
        var data = get(key);
        if (data != null)
            return (T) data;

        T loaded = loader.get();
        setWithTimeout(key, loaded, ttl, TimeUnit.MILLISECONDS);
        return loaded;
    }

    @SneakyThrows
    public <T> T getAsideMinute(String key, Supplier<T> loader, int ttl) {
        var data = get(key);
        if (data != null)
            return (T) data;

        T loaded = loader.get();
        setWithTimeout(key, loaded, ttl, TimeUnit.MINUTES);
        return loaded;
    }

    @SneakyThrows
    public <T> T getAsideDay(String key, Supplier<T> loader, int ttl) {
        var data = get(key);
        if (data != null)
            return (T) data;

        T loaded = loader.get();
        setWithTimeout(key, loaded, ttl, TimeUnit.DAYS);
        return loaded;
    }

    @SneakyThrows
    public <T> T getAsideTemplate(String format, String key, Supplier<T> loader, int ttl, TimeUnit timeUnit) {
        String keyFinal = String.format(format, key);
        var data = get(keyFinal);
        if (data != null)
            return (T) data;
        T loaded = loader.get();
        setWithTimeout(keyFinal, loaded, ttl, timeUnit);
        return loaded;
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

    // ===================================================================
    // zSetOps
    // ===================================================================

    private String getKey(String category) {
        return "leaderboard:" + category;
    }

    // Add or update score
    public <T> void addScore(String category, T member, double score) {
        zSetOps.add(getKey(category), member, score);
    }

    // Increase score ZSCORE
    public <T> void incrementScore(String category, T member, double delta) {
        zSetOps.incrementScore(getKey(category), member, delta);
    }

    // Get user's rank ZREVRANK
    public <T> Long getRank(String category, T member) {
        Long rank = zSetOps.reverseRank(getKey(category), member);
        return rank == null ? null : rank + 1; // convert to 1-based rank
    }

    // Get user's score
    public <T> Double getScore(String category, T member) {
        return zSetOps.score(getKey(category), member);
    }

    // Get top N members ZREVRANGE
    public Set<ZSetOperations.TypedTuple<Object>> getTopN(String category, int n) {
        return zSetOps.reverseRangeWithScores(getKey(category), 0, n - 1);
    }

    // Get members around a given user ZREVRANGE
    public <T> Set<ZSetOperations.TypedTuple<Object>> getAroundUser(String category, T member, int range) {
        Long rank = zSetOps.reverseRank(getKey(category), member);
        if (rank == null) return Set.of();
        long start = Math.max(rank - range, 0);
        long end = rank + range;
        return zSetOps.reverseRangeWithScores(getKey(category), start, end);
    }

    // Remove one member
    public <T> void removeMember(String category, T member) {
        zSetOps.remove(getKey(category), member);
    }
}
