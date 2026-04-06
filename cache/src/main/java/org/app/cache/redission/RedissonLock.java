package org.app.cache.redission;

import org.redisson.api.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed synchronization primitives via Redisson.
 * <p>
 * All locks use Lua scripts for atomic operations — far safer than manual
 * SET NX EX implementations. Redisson also handles automatic lock renewal
 * (watchdog) so locks don't expire while the holder is still working.
 *
 * <pre>{@code
 * RedissonLock locks = RedissonLock.of(factory);
 *
 * // Simple lock
 * locks.withLock("payment:42", () -> processPayment());
 *
 * // Lock with retry + timeout
 * locks.withLock("order:1", 5, Duration.ofMillis(200), () -> processOrder());
 *
 * // Lock returning value
 * String result = locks.withLock("key", () -> compute());
 *
 * // Fair lock (FIFO ordering guaranteed)
 * locks.withFairLock("resource", () -> access());
 *
 * // Read/Write lock
 * locks.withReadLock("data",  () -> readData());
 * locks.withWriteLock("data", () -> writeData());
 *
 * // Multi-lock (lock multiple keys atomically)
 * locks.withMultiLock(() -> doWork(), "lock:a", "lock:b", "lock:c");
 *
 * // Semaphore (limit concurrent access)
 * locks.withSemaphore("db-pool", 5, () -> callDatabase());
 *
 * // Async lock
 * locks.withLockAsync("key", () -> CompletableFuture.supplyAsync(() -> work()));
 * }</pre>
 */
public final class RedissonLock {

    private final RedissonClient client;

    private RedissonLock(RedissonClient client) {
        this.client = client;
    }

    public static RedissonLock of(RedissonFactory factory) {
        return new RedissonLock(factory.client());
    }

    // =========================================================================
    // Standard Lock (reentrant, watchdog auto-renewal)
    // =========================================================================

    /**
     * Lock, run, unlock. Watchdog keeps lock alive while action runs.
     * No lease time limit — safe for long operations.
     */
    public void withLock(String key, Runnable action) {
        RLock lock = client.getLock(key);
        lock.lock();
        try { action.run(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    public <T> T withLock(String key, Supplier<T> action) {
        RLock lock = client.getLock(key);
        lock.lock();
        try { return action.get(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    /**
     * Try to acquire lock, retry up to {@code maxAttempts} times.
     * Each tryLock waits up to {@code waitTime} before giving up that attempt.
     * Lock auto-expires after {@code leaseTime} (no watchdog — explicit TTL).
     *
     * @throws LockNotAcquiredException if all attempts fail
     */
    public void withLock(String key, int maxAttempts, Duration waitTime, Runnable action) {
        withLock(key, maxAttempts, waitTime, Duration.ZERO, action);
    }

    public void withLock(String key, int maxAttempts, Duration waitTime, Duration leaseTime, Runnable action) {
        RLock lock = client.getLock(key);
        boolean acquired = false;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                long lease = leaseTime.isZero() ? -1 : leaseTime.toMillis();
                acquired = lock.tryLock(waitTime.toMillis(), lease, TimeUnit.MILLISECONDS);
                if (acquired) break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockNotAcquiredException("Interrupted while acquiring lock: " + key, e);
            }
        }
        if (!acquired) throw new LockNotAcquiredException("Could not acquire lock after " + maxAttempts + " attempts: " + key);
        try { action.run(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    public <T> T withLock(String key, int maxAttempts, Duration waitTime, Supplier<T> action) {
        RLock lock = client.getLock(key);
        boolean acquired = false;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                acquired = lock.tryLock(waitTime.toMillis(), -1, TimeUnit.MILLISECONDS);
                if (acquired) break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockNotAcquiredException("Interrupted: " + key, e);
            }
        }
        if (!acquired) throw new LockNotAcquiredException("Could not acquire lock: " + key);
        try { return action.get(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    /** Direct try — single attempt, no retry. Returns false if not acquired. */
    public boolean tryLock(String key, Duration waitTime) {
        try {
            return client.getLock(key).tryLock(waitTime.toMillis(), -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String key) {
        RLock lock = client.getLock(key);
        if (lock.isHeldByCurrentThread()) lock.unlock();
    }

    public boolean isLocked(String key) {
        return client.getLock(key).isLocked();
    }

    public RLock rlock(String key) { return client.getLock(key); }

    // =========================================================================
    // Fair Lock (FIFO thread ordering)
    // =========================================================================

    /**
     * Fair lock guarantees lock acquisition in request-arrival order.
     * No starvation — every waiter eventually gets the lock.
     */
    public void withFairLock(String key, Runnable action) {
        RLock lock = client.getFairLock(key);
        lock.lock();
        try { action.run(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    public <T> T withFairLock(String key, Supplier<T> action) {
        RLock lock = client.getFairLock(key);
        lock.lock();
        try { return action.get(); }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }

    public RLock rfairLock(String key) { return client.getFairLock(key); }

    // =========================================================================
    // Read/Write Lock
    // =========================================================================

    /**
     * Read lock — multiple readers can hold simultaneously.
     * Blocks if a write lock is held.
     */
    public void withReadLock(String key, Runnable action) {
        RReadWriteLock rwLock = client.getReadWriteLock(key);
        RLock readLock = rwLock.readLock();
        readLock.lock();
        try { action.run(); }
        finally { if (readLock.isHeldByCurrentThread()) readLock.unlock(); }
    }

    public <T> T withReadLock(String key, Supplier<T> action) {
        RReadWriteLock rwLock = client.getReadWriteLock(key);
        RLock readLock = rwLock.readLock();
        readLock.lock();
        try { return action.get(); }
        finally { if (readLock.isHeldByCurrentThread()) readLock.unlock(); }
    }

    /**
     * Write lock — exclusive. Blocks all readers and other writers.
     */
    public void withWriteLock(String key, Runnable action) {
        RReadWriteLock rwLock = client.getReadWriteLock(key);
        RLock writeLock = rwLock.writeLock();
        writeLock.lock();
        try { action.run(); }
        finally { if (writeLock.isHeldByCurrentThread()) writeLock.unlock(); }
    }

    public <T> T withWriteLock(String key, Supplier<T> action) {
        RReadWriteLock rwLock = client.getReadWriteLock(key);
        RLock writeLock = rwLock.writeLock();
        writeLock.lock();
        try { return action.get(); }
        finally { if (writeLock.isHeldByCurrentThread()) writeLock.unlock(); }
    }

    public RReadWriteLock rreadWriteLock(String key) { return client.getReadWriteLock(key); }

    // =========================================================================
    // MultiLock — lock multiple keys atomically (no partial lock state)
    // =========================================================================

    /**
     * Acquire all listed locks atomically before running action.
     * Either ALL locks are acquired or NONE — prevents deadlock scenarios.
     *
     * <pre>
     * locks.withMultiLock(() -> transfer(from, to), "account:1", "account:2");
     * </pre>
     */
    public void withMultiLock(Runnable action, String... keys) {
        RLock[] lockArray = java.util.Arrays.stream(keys)
            .map(client::getLock)
            .toArray(RLock[]::new);
        RLock multiLock = client.getMultiLock(lockArray);
        multiLock.lock();
        try { action.run(); }
        finally { if (multiLock.isHeldByCurrentThread()) multiLock.unlock(); }
    }

    public <T> T withMultiLock(Supplier<T> action, String... keys) {
        RLock[] lockArray = java.util.Arrays.stream(keys)
            .map(client::getLock)
            .toArray(RLock[]::new);
        RLock multiLock = client.getMultiLock(lockArray);
        multiLock.lock();
        try { return action.get(); }
        finally { if (multiLock.isHeldByCurrentThread()) multiLock.unlock(); }
    }

    // =========================================================================
    // Semaphore (limit concurrent access across JVMs)
    // =========================================================================

    /**
     * Semaphore with fixed permit count.
     * <p>
     * First call to this method on a new key sets the permit count.
     * Subsequent calls with different permit counts are ignored.
     *
     * <pre>
     * // Max 5 concurrent DB connections across all instances
     * locks.withSemaphore("db-pool", 5, () -> callDatabase());
     * </pre>
     */
    public void withSemaphore(String key, int permits, Runnable action) {
        RSemaphore sem = client.getSemaphore(key);
        sem.trySetPermits(permits);
        try {
            sem.acquire();
            try { action.run(); }
            finally { sem.release(); }
        } catch (Exception e) {
            throw new RedissonOps.RedissonException("Semaphore error: " + key, e);
        }
    }

    public <T> T withSemaphore(String key, int permits, Supplier<T> action) throws InterruptedException {
        RSemaphore sem = client.getSemaphore(key);
        sem.trySetPermits(permits);
        sem.acquire();
        try { return action.get(); }
        finally { sem.release(); }
    }

    /** Try to acquire semaphore with timeout. Returns false if not acquired. */
    public boolean trySemaphore(String key, int permits, Duration timeout) {
        RSemaphore sem = client.getSemaphore(key);
        sem.trySetPermits(permits);
        try {
            return sem.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public RSemaphore rsemaphore(String key, int permits) {
        RSemaphore sem = client.getSemaphore(key);
        sem.trySetPermits(permits);
        return sem;
    }

    // =========================================================================
    // CountDownLatch — cross-JVM synchronization point
    // =========================================================================

    /**
     * Set the count. Must be called once by the coordinator before any await.
     */
    public void countDownLatchSet(String key, int count) {
        client.getCountDownLatch(key).trySetCount(count);
    }

    /** Count down by 1. */
    public void countDown(String key) {
        client.getCountDownLatch(key).countDown();
    }

    /** Block until count reaches 0. */
    public void awaitLatch(String key) {
        try { client.getCountDownLatch(key).await(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public boolean awaitLatch(String key, Duration timeout) {
        try { return client.getCountDownLatch(key).await(timeout.toMillis(), TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
    }

    public RCountDownLatch rcountDownLatch(String key) { return client.getCountDownLatch(key); }

    // =========================================================================
    // Async lock
    // =========================================================================

    /**
     * Async lock — acquire lock, run async action, release when done.
     *
     * <pre>
     * locks.withLockAsync("key", () -> CompletableFuture.supplyAsync(() -> doWork()))
     *      .thenAccept(result -> log.info("Done: {}", result));
     * </pre>
     */
    public <T> CompletableFuture<T> withLockAsync(String key, Supplier<CompletableFuture<T>> asyncAction) {
        RLock lock = client.getLock(key);
        return lock.lockAsync().toCompletableFuture()
            .thenCompose(v -> asyncAction.get()
                .whenComplete((r, ex) -> {
                    if (lock.isHeldByCurrentThread()) lock.unlock();
                }));
    }

    // =========================================================================
    // Exception
    // =========================================================================

    public static final class LockNotAcquiredException extends RuntimeException {
        private static final long serialVersionUID = 3336582131186536640L;

        public LockNotAcquiredException(String msg)                  { super(msg); }
        public LockNotAcquiredException(String msg, Throwable cause) { super(msg, cause); }
    }
}
