package org.app.cache;

import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

public class CacheManagerUtils {
    public static final String RATE_LIMIT_CACHE = "rate-limit-buckets";

    /// JCache with Redisson for Bucket
    public static CacheManager getCacheManagerForBucket(Config config) {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        // JCache from config Redisson
        Configuration<Object, Object> jcacheConfig = RedissonConfiguration.fromConfig(config);

        // create first if not maybe meet "cache not found"
        if (cacheManager.getCache(RATE_LIMIT_CACHE) == null) {
            cacheManager.createCache(RATE_LIMIT_CACHE, jcacheConfig);
        }

        return cacheManager;
    }
}
