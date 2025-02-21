package org.app.common.utils;

import lombok.SneakyThrows;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

public class CacheUtils {
    @SneakyThrows
    public static <T> T putCache(T data, Callable<T> callable, Cache cache, Object key) {
        if (data == null) {
            T t = callable.call();
            cache.put(key, t);
            return t;
        }
        return data;
    }


    @SneakyThrows
    public static <T> T putCache(T data, Callable<T> callable, Cache cache, Object key, long ttlInSeconds) {
        if (data == null) {
            T t = callable.call();
            cache.put(key, t);
            scheduleExpiry(cache, key, ttlInSeconds);
            return t;
        }
        return data;
    }

    private static void scheduleExpiry(Cache cache, Object key, long ttlInSeconds) {
        new Thread(() -> {
            try {
                Thread.sleep(ttlInSeconds * 1000L);
                cache.evict(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
