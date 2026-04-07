package org.app.concurrent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Utility class for safely executing code blocks with a lock mechanism.
 * <p>
 * This class provides methods to execute code blocks (either {@link Runnable} or {@link Supplier})
 * within a {@link ReentrantLock}, ensuring thread safety.
 * </p>
 * <p>
 * The class is designed to be non-instantiable and provides static utility methods.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Lock {

    /**
     * Executes the given {@link Supplier} within a {@link ReentrantLock}, ensuring thread safety.
     * <p>
     * This method locks the {@link ReentrantLock}, executes the provided function, and then
     * unlocks the lock, ensuring that the lock is always released even if an exception occurs.
     * </p>
     *
     * @param function the function to execute within the lock
     * @param <T>      the type of the result returned by the function
     * @return the result of the function execution
     * @throws RuntimeException if the function throws an exception
     */
    @SneakyThrows
    public static <T> T around(Supplier<T> function) {
        final java.util.concurrent.locks.Lock lock = new ReentrantLock();
        lock.lock();
        try {
            return function.get(); // Execute the passed function safely
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes the given {@link Runnable} within a {@link ReentrantLock}, ensuring thread safety.
     * <p>
     * This method locks the {@link ReentrantLock}, executes the provided function, and then
     * unlocks the lock, ensuring that the lock is always released even if an exception occurs.
     * </p>
     *
     * @param function the function to execute within the lock
     */
    public static void around(Runnable function) {
        final java.util.concurrent.locks.Lock lock = new ReentrantLock();
        lock.lock();
        try {
            function.run(); // Execute the function safely
        } finally {
            lock.unlock();
        }
    }
}
