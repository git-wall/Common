package org.app.common.action;

import com.google.common.hash.Funnels;
import org.app.common.db.QuerySupplier;
import org.app.common.filter.ScalableBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

// ScalableBloomFilter → RedisTemplate (cache) → Real DB (query)
// supper fast -> fast -> slow(safe)
// Action: check if user already exists
public class BloomAction<T> {

    @Value("${bloom.filter.size}")
    private int bloomFilterSize = 10_000;

    private final RedisTemplate<String, String> redisTemplate;

    private final ScalableBloomFilter<CharSequence> bloomFilter;

    private QuerySupplier<T> query;

    public BloomAction(RedisTemplate<String, String> redisTemplate, int bloomSize) {
        this.redisTemplate = redisTemplate;
        this.bloomFilter = new ScalableBloomFilter<>(
                bloomSize != 0 ? bloomSize : bloomFilterSize,
                Funnels.stringFunnel(StandardCharsets.UTF_8)
        );
    }

    public BloomAction<T> query(QuerySupplier<T> query) {
        this.query = query;
        return this;
    }

    public BloomAction<T> load() {
        // Load data from DB to Redis and Bloom
        query.getFindFields()
                .get()
                .forEach(e -> {
                    redisTemplate.opsForValue().set(e, e);
                    bloomFilter.add(e);
                });
        return this;
    }

    public boolean exists(String element) {
        if (!bloomFilter.mightContain(element)) {
            return false;
        }

        if (redisTemplate.opsForValue().get(element) != null) {
            return true;
        }

        return query.getExits().get();
    }

    public T register(String element, String message) {
        if (exists(element)) {
            throw new IllegalArgumentException(message);
        }

        // Insert into DB, Redis and Bloom
        redisTemplate.opsForValue().set(element, element);
        bloomFilter.add(element);
        return query.getInsert().get();
    }
}
