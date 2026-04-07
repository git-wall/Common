package org.app.cache.jedis;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Simple distributed lock backed by Redis SET NX EX.
 * <p>
 * Each lock acquisition stores a unique token so only the owner can release it.
 *
 * <pre>{@code
 * RedisLock lock = RedisLock.of(ops, "lock:payment:42", 10);
 *
 * // Try once
 * boolean acquired = lock.tryLock();
 *
 * // Try with retry
 * boolean acquired = lock.tryLock(5, 200); // 5 attempts, 200ms apart
 *
 * // Execute block while holding lock
 * lock.withLock(() -> {
 *     processPayment();
 * });
 *
 * // Always release in finally
 * lock.unlock();
 * }</pre>
 */
public final class RedisLock {

    private final RedisOps ops;
    private final String lockKey;
    private final long ttlSeconds;
    private String token; // current owner token

    private RedisLock(RedisOps ops, String lockKey, long ttlSeconds) {
        this.ops = ops;
        this.lockKey = lockKey;
        this.ttlSeconds = ttlSeconds;
    }

    /** @param ttlSeconds auto-expiry of the lock to prevent deadlocks */
    public static RedisLock of(RedisOps ops, String lockKey, long ttlSeconds) {
        return new RedisLock(ops, lockKey, ttlSeconds);
    }

    /** Single attempt. Returns true if lock acquired. */
    public boolean tryLock() {
        String t = UUID.randomUUID().toString();
        boolean ok = ops.setNx(lockKey, t, ttlSeconds);
        if (ok) this.token = t;
        return ok;
    }

    /**
     * Retry up to {@code maxAttempts} times with {@code retryIntervalMs} delay.
     */
    public boolean tryLock(int maxAttempts, long retryIntervalMs) {
        for (int i = 0; i < maxAttempts; i++) {
            if (tryLock()) return true;
            if (i < maxAttempts - 1) {
                try { Thread.sleep(retryIntervalMs); } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Release the lock only if this instance owns it (token matches).
     * Safe against releasing another process's lock.
     */
    public void unlock() {
        if (token == null) return;
        // Atomic check-and-delete via optimistic approach:
        // For full atomicity in production use a Lua script.
        ops.get(lockKey).ifPresent(val -> {
            if (token.equals(val)) ops.del(lockKey);
        });
        token = null;
    }

    /**
     * Acquire lock, run action, release lock. Throws {@link LockNotAcquiredException}
     * if lock cannot be obtained after {@code maxAttempts}.
     */
    public void withLock(int maxAttempts, long retryIntervalMs, Runnable action) {
        if (!tryLock(maxAttempts, retryIntervalMs)) {
            throw new LockNotAcquiredException("Could not acquire lock: " + lockKey);
        }
        try {
            action.run();
        } finally {
            unlock();
        }
    }

    /** Single-attempt version of {@link #withLock}. */
    public void withLock(Runnable action) {
        withLock(1, 0, action);
    }

    /** Single-attempt version returning a result. */
    public <T> T withLock(Supplier<T> action) {
        if (!tryLock()) throw new LockNotAcquiredException("Could not acquire lock: " + lockKey);
        try {
            return action.get();
        } finally {
            unlock();
        }
    }

    public boolean isLocked() {
        return ops.exists(lockKey);
    }

    public static final class LockNotAcquiredException extends RuntimeException {
        private static final long serialVersionUID = -5207152134100491449L;

        public LockNotAcquiredException(String msg) { super(msg); }
    }
}
