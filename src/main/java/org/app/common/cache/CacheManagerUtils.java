package org.app.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

public class CacheManagerUtils {

    public static CacheManager simpleCacheManager(Collection<? extends Cache> caches) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    public static CacheManager concurrentMapCacheManager(String... cacheNames) {
        return new ConcurrentMapCacheManager(cacheNames);
    }

    public static CacheManager caffeineCacheManager(Duration ttl, long maximumSize, String... cacheNames) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(cacheNames);

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .recordStats();

        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    public static CacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                                 Duration ttl,
                                                 String... cacheNames) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .initialCacheNames(Set.of(cacheNames))
                .build();
    }
}
