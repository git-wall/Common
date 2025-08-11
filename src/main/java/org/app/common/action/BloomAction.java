package org.app.common.action;

import com.google.common.hash.Funnels;
import org.app.common.db.QuerySupplier;
import org.app.common.filter.ScalableBloomFilter;
import org.app.common.utils.PasswordCrypto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

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

    public BloomAction<T> load(List<T> items) {
        items.stream()
                .map(Object::hashCode)
                .map(PasswordCrypto::hash)
                .forEach(hash -> {
                    redisTemplate.opsForValue().set(hash, UUID.randomUUID().toString());
                    bloomFilter.add(hash);
                });
        return this;
    }

    public boolean exists(String hash) {
        if (bloomFilter.mightContain(hash)) {
            return true;
        }

        if (redisTemplate.opsForValue().get(hash) != null) {
            return true;
        }

        if (query != null) {
            return query.getExits().get();
        }

        return false;
    }

    public T register(T element, String message) {
        String hash = PasswordCrypto.hash(element.hashCode());
        if (exists(hash)) {
            throw new IllegalArgumentException(message);
        }

        // Insert into DB, Redis and Bloom
        redisTemplate.opsForValue().set(hash, UUID.randomUUID().toString());
        bloomFilter.add(hash);

        if (query != null) {
            return query.getInsert().get();
        }

        return element;
    }

    public T register(T element) {
        String hash = PasswordCrypto.hash(element.hashCode());

        if (exists(hash)) {
            return null;
        }

        // Insert into DB, Redis and Bloom
        redisTemplate.opsForValue().set(hash, UUID.randomUUID().toString());
        bloomFilter.add(hash);

        if (query != null) {
            return query.getInsert().get();
        }

        return element;
    }
}
