package org.app.common.design.legacy;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A dynamic Backpressure Pattern implementation that limits the rate of operations
 * to prevent system overload. This implementation supports dynamic configuration
 * of permits, timeout, and rejection strategies.
 */
public class Backpressure<T> {

    private final Semaphore semaphore;
    private final AtomicInteger activeOperations = new AtomicInteger(0);
    private final AtomicInteger rejectedOperations = new AtomicInteger(0);
    private final AtomicInteger totalOperations = new AtomicInteger(0);
    private volatile long timeoutMillis;
    private volatile RejectionStrategy rejectionStrategy;

    /**
     * Strategy to handle rejected operations when backpressure is applied.
     */
    public enum RejectionStrategy {
        /**
         * Throw an exception when operation is rejected
         */
        THROW_EXCEPTION,

        /**
         * Return a default value when operation is rejected
         */
        RETURN_DEFAULT,

        /**
         * Block until a permit becomes available (ignores timeout)
         */
        BLOCK_INDEFINITELY
    }

    /**
     * Creates a new Backpressure instance with the specified number of permits and timeout.
     *
     * @param permits Maximum number of concurrent operations allowed
     * @param timeoutMillis Maximum time to wait for a permit in milliseconds
     * @param rejectionStrategy Strategy to handle rejected operations
     */
    public Backpressure(int permits, long timeoutMillis, RejectionStrategy rejectionStrategy) {
        this.semaphore = new Semaphore(permits, true);
        this.timeoutMillis = timeoutMillis;
        this.rejectionStrategy = rejectionStrategy;
    }

    /**
     * Creates a new Backpressure instance with default rejection strategy (THROW_EXCEPTION).
     *
     * @param permits Maximum number of concurrent operations allowed
     * @param timeoutMillis Maximum time to wait for a permit in milliseconds
     */
    public Backpressure(int permits, long timeoutMillis) {
        this(permits, timeoutMillis, RejectionStrategy.THROW_EXCEPTION);
    }

    /**
     * Executes the given supplier with backpressure applied.
     *
     * @param supplier The operation to execute
     * @param defaultValue The default value to return if operation is rejected and using RETURN_DEFAULT strategy
     * @return The result of the operation or the default value if rejected
     * @throws BackpressureException If the operation is rejected and using THROW_EXCEPTION strategy
     */
    public T execute(Supplier<T> supplier, T defaultValue) throws BackpressureException {
        totalOperations.incrementAndGet();
        boolean acquired = false;

        try {
            if (rejectionStrategy == RejectionStrategy.BLOCK_INDEFINITELY) {
                semaphore.acquire();
                acquired = true;
            } else {
                acquired = semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
            }

            if (acquired) {
                activeOperations.incrementAndGet();
                try {
                    return supplier.get();
                } finally {
                    activeOperations.decrementAndGet();
                }
            } else {
                rejectedOperations.incrementAndGet();
                if (rejectionStrategy == RejectionStrategy.THROW_EXCEPTION) {
                    throw new BackpressureException("Operation rejected due to backpressure");
                }
                return defaultValue;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rejectedOperations.incrementAndGet();
            if (rejectionStrategy == RejectionStrategy.THROW_EXCEPTION) {
                throw new BackpressureException("Operation interrupted while waiting for permit", e);
            }
            return defaultValue;
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    /**
     * Executes the given supplier with backpressure applied.
     *
     * @param supplier The operation to execute
     * @return The result of the operation
     * @throws BackpressureException If the operation is rejected
     */
    public T execute(Supplier<T> supplier) throws BackpressureException {
        return execute(supplier, null);
    }

    /**
     * Executes the given runnable with backpressure applied.
     *
     * @param runnable The operation to execute
     * @throws BackpressureException If the operation is rejected
     */
    public void execute(Runnable runnable) throws BackpressureException {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Dynamically updates the number of permits.
     *
     * @param newPermits The new number of permits
     */
    public void updatePermits(int newPermits) {
        int currentPermits = semaphore.availablePermits();
        if (newPermits > currentPermits) {
            semaphore.release(newPermits - currentPermits);
        } else if (newPermits < currentPermits) {
            semaphore.acquireUninterruptibly(currentPermits - newPermits);
        }
    }

    /**
     * Dynamically updates the timeout value.
     *
     * @param timeoutMillis The new timeout in milliseconds
     */
    public void updateTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Dynamically updates the rejection strategy.
     *
     * @param rejectionStrategy The new rejection strategy
     */
    public void updateRejectionStrategy(RejectionStrategy rejectionStrategy) {
        this.rejectionStrategy = rejectionStrategy;
    }

    /**
     * Gets the current number of active operations.
     *
     * @return The number of active operations
     */
    public int getActiveOperations() {
        return activeOperations.get();
    }

    /**
     * Gets the current number of rejected operations.
     *
     * @return The number of rejected operations
     */
    public int getRejectedOperations() {
        return rejectedOperations.get();
    }

    /**
     * Gets the total number of operations attempted.
     *
     * @return The total number of operations
     */
    public int getTotalOperations() {
        return totalOperations.get();
    }

    /**
     * Gets the current rejection rate (rejected operations / total operations).
     *
     * @return The rejection rate as a value between 0.0 and 1.0
     */
    public double getRejectionRate() {
        int total = totalOperations.get();
        return total > 0 ? (double) rejectedOperations.get() / total : 0.0;
    }

    /**
     * Exception thrown when an operation is rejected due to backpressure.
     */
    public static class BackpressureException extends RuntimeException {
        private static final long serialVersionUID = 1724484731508200938L;

        public BackpressureException(String message) {
            super(message);
        }

        public BackpressureException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
