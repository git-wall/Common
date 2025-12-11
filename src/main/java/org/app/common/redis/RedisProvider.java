package org.app.common.redis;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
public class RedisProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 🟢 Set generic value
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    // 🟡 Get generic value
    public <T> T get(String key, Class<T> type) {
        Object val = redisTemplate.opsForValue().get(key);
        return val == null ? null : type.cast(val);
    }

    // 🔵 Delete key
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 🔹 Check exist
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // 🧩 Pipeline Executor
    public List<Object> pipeline(List<Runnable> operations) {
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            operations.forEach(Runnable::run);
            return null;
        });
    }

    // ⚡ Async Executor
    public CompletableFuture<Void> async(Runnable action) {
        return CompletableFuture.runAsync(action);
    }

    // 🧮 Increment
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // 🧾 Sorted Set basic ops
    public void zadd(String key, Object member, double score) {
        redisTemplate.opsForZSet().add(key, member, score);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Long zrank(String key, Object member) {
        return redisTemplate.opsForZSet().rank(key, member);
    }
}
