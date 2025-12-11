package org.app.common.usecase;


import lombok.RequiredArgsConstructor;
import org.app.common.redis.RedisAsyncExecutor;
import org.app.common.redis.RedisCompressor;
import org.app.common.redis.RedisProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisProvider provider;
    private final RedisAsyncExecutor asyncExecutor;

    // 🟢 Put object (with optional compression)
    public <T> void put(String key, T data, Duration ttl, boolean compress) {
        try {
            if (compress) {
                byte[] compressed = RedisCompressor.compress(data);
                provider.set(key, compressed, ttl);
            } else {
                provider.set(key, data, ttl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cache put failed for key: " + key, e);
        }
    }

    // 🔵 Async put
    public <T> void putAsync(String key, T data, Duration ttl, boolean compress) {
        asyncExecutor.submitVoid(() -> put(key, data, ttl, compress));
    }

    // 🟡 Get object (auto decompress if needed)
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, boolean compressed, Integer originalLength) {
        Object raw = provider.get(key, Object.class);
        if (raw == null) return null;

        try {
            if (!compressed) return (T) raw;

            byte[] data = (byte[]) raw;
            return RedisCompressor.decompress(data, originalLength, type);
        } catch (Exception e) {
            throw new RuntimeException("Cache get failed for key: " + key, e);
        }
    }

    // ⚡ Multi get (async parallel)
    public <T> Map<String, T> multiGetAsync(List<String> keys, Class<T> type, boolean compressed, Integer originalLength) {
        Map<String, T> result = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String key : keys) {
            futures.add(asyncExecutor.submitVoid(() -> {
                T value = get(key, type, compressed, originalLength);
                if (value != null) result.put(key, value);
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return result;
    }

    // 🧹 Delete
    public void clear(String key) {
        provider.delete(key);
    }
}
