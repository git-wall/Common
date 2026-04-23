package org.app.cache.lock;

import com.google.common.util.concurrent.Striped;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class StripedLock {
    private final Striped<Lock> stripedLock;

    public StripedLock(int buckets) {
        stripedLock = Striped.lock(buckets);
    }

    private Supplier<?> putSupplier(Map<String,String> map, int key) {
        return (()-> {
//            int bucket = key % stripedLock.size();
            Lock lock = stripedLock.get(key);
            lock.lock();
            try {
                return map.put("key" + key, "value" + key);
            } finally {
                lock.unlock();
            }
        });
    }

    private Supplier<?> getSupplier(Map<String,String> map, int key) {
        return (()-> {
            int bucket = key % stripedLock.size();
            Lock lock = stripedLock.get(bucket);
            lock.lock();
            try {
                return map.get("key" + key);
            } finally {
                lock.unlock();
            }
        });
    }
}
