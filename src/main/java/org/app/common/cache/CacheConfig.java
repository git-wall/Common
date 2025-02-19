package org.app.common.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Configuration
public class CacheConfig {

    @NonNull
    public static <K, V> LoadingCache<K, V> cacheTTL(long timeout, TimeUnit timeUnit, Function<K, V> function) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, timeUnit)
                .maximumSize(Runtime.getRuntime().maxMemory() / (long) (1024 * 1024 * 10))
                .build(new CacheLoader<>() {
                    @NonNull
                    @Override
                    public V load(@NonNull K key) {
                        return function.apply(key);
                    }
                });
    }

}