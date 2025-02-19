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
}
