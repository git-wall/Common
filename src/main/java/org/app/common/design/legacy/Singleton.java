package org.app.common.design.legacy;

import java.util.function.Supplier;

/**
 * Singleton - Example
 * <pre>{@code
 * Singleton<CacheConfig> cacheConfig = new Singleton<>();
 * CacheConfig config = cacheConfig.getInstance(CacheConfig::new);
 * }</pre>
 * */
public class Singleton<T> {

    private volatile T instance;
    private final Object lock = new Object();

    public T getInstance(Supplier<T> supplier) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = supplier.get();
                }
            }
        }
        return instance;
    }
}
