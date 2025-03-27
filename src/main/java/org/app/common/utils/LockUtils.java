package org.app.common.utils;

import lombok.SneakyThrows;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LockUtils {

    @SneakyThrows
    public static <T> T around(Supplier<T> function) {
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            return function.get(); // Execute the passed function safely
        } finally {
            lock.unlock();
        }
    }

    public static void around(Runnable function) {
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            function.run(); // Execute the function safely
        } finally {
            lock.unlock();
        }
    }
}
