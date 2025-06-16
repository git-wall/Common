package org.app.common.design.legacy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final BlockingQueue<T> pool;
    private final Supplier<T> factory;

    public ObjectPool(int size, Supplier<T> factory) {
        this.factory = factory;
        this.pool = new LinkedBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            pool.offer(factory.get());
        }
    }

    public T acquire() throws InterruptedException {
        T object = pool.poll();
        return object != null ? object : factory.get();
    }

    public void release(T object) {
        if (object != null) {
            pool.offer(object);
        }
    }
}