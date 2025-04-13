package org.app.common.db.map;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MemoryCache {
    private final MapDbProperties properties;
    private final ConcurrentHashMap<String, Cache<String, String>> caches = new ConcurrentHashMap<>();

    public MemoryCache(MapDbProperties properties) {
        this.properties = properties;
    }

    public void createCache(String tableName) {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getCache().getTtl(), TimeUnit.SECONDS)
                .maximumSize(10_000L)
                .build();
        caches.put(tableName, cache);
    }

    public void put(String tableName, String key, String value) {
        if (caches.containsKey(tableName)) caches.get(tableName).put(key, value);
    }

    public String get(String tableName, String key) {
        return caches.containsKey(tableName) ? caches.get(tableName).getIfPresent(key) : null;
    }

    public void remove(String table, String key) {
        caches.get(table).invalidate(key);
    }
}
