package org.app.cache.lettuce;

import io.lettuce.core.ScriptOutputType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Distributed lock backed by Redis SET NX EX via Lettuce.
 * <p>
 * Uses a unique token per acquisition so only the owner can release the lock.
 * Unlock is atomic via a Lua script (check-and-delete).
 *
 * <pre>{@code
 * LettuceLock lock = LettuceLock.of(ops, "lock:order:42", 10);
 *
 * // Try once
 * boolean acquired = lock.tryLock();
 *
 * // Retry
 * boolean acquired = lock.tryLock(5, 200);
 *
 * // Run block while holding lock (sync)
 * lock.withLock(() -> processOrder());
 *
 * // Run block while holding lock (async)
 * lock.withLockAsync(() -> CompletableFuture.supplyAsync(() -> processOrder()));
 *
 * // Manual release
 * lock.unlock();
 * }</pre>
 */
public final class LettuceLock {

    /**
     * Lua script for atomic check-and-delete.
     * Returns 1 if deleted, 0 if token didn't match (we don't own the lock).
     */
    private static final String UNLOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    private final LettuceOps ops;
    private final LettuceAsyncOps asyncOps;
    private final String          lockKey;
    private final long            ttlSeconds;
    private volatile String       token;

    private LettuceLock(LettuceOps ops, String lockKey, long ttlSeconds) {
        this.ops        = ops;
        this.asyncOps   = LettuceAsyncOps.of(ops.factory());
        this.lockKey    = lockKey;
        this.ttlSeconds = ttlSeconds;
    }

    public static LettuceLock of(LettuceOps ops, String lockKey, long ttlSeconds) {
        return new LettuceLock(ops, lockKey, ttlSeconds);
    }

    // -------------------------------------------------------------------------
    // Sync
    // -------------------------------------------------------------------------

    /** Single attempt to acquire the lock. Returns true if successful. */
    public boolean tryLock() {
        String t = UUID.randomUUID().toString();
        boolean ok = ops.setNx(lockKey, t, ttlSeconds);
        if (ok) this.token = t;
        return ok;
    }

    /** Retry up to {@code maxAttempts} times, sleeping {@code retryIntervalMs} between each. */
    public boolean tryLock(int maxAttempts, long retryIntervalMs) {
        for (int i = 0; i < maxAttempts; i++) {
            if (tryLock()) return true;
            if (i < maxAttempts - 1) {
                try { Thread.sleep(retryIntervalMs); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Atomic unlock via Lua script — only releases if this instance owns the lock.
     * Safe against accidentally releasing another process's lock.
     *
     * @return true if the lock was released by this call
     */
    public boolean unlock() {
        if (token == null) return false;
        try (var factory = ops.factory()) {
            Long result = factory.sync().eval(
                UNLOCK_SCRIPT, ScriptOutputType.INTEGER,
                new String[]{ lockKey }, token
            );
            return result != null && result == 1L;
        } finally {
            token = null;
        }
    }

    /**
     * Acquire lock, run action, unconditionally release in finally.
     * Throws {@link LockNotAcquiredException} if cannot acquire.
     */
    public void withLock(Runnable action) {
        withLock(1, 0, action);
    }

    public void withLock(int maxAttempts, long retryIntervalMs, Runnable action) {
        if (!tryLock(maxAttempts, retryIntervalMs))
            throw new LockNotAcquiredException("Could not acquire lock: " + lockKey);
        try { action.run(); }
        finally { unlock(); }
    }

    public <T> T withLock(Supplier<T> action) {
        if (!tryLock()) throw new LockNotAcquiredException("Could not acquire lock: " + lockKey);
        try { return action.get(); }
        finally { unlock(); }
    }

    // -------------------------------------------------------------------------
    // Async
    // -------------------------------------------------------------------------

    /** Async tryLock — returns CompletableFuture<Boolean>. */
    public CompletableFuture<Boolean> tryLockAsync() {
        String t = UUID.randomUUID().toString();
        return asyncOps.setNx(lockKey, t, ttlSeconds)
            .thenApply(ok -> { if (ok) this.token = t; return ok; });
    }

    /**
     * Async withLock — acquires lock, runs async action, releases in thenCompose.
     *
     * <pre>
     * lock.withLockAsync(() -> CompletableFuture.supplyAsync(() -> processOrder()))
     *     .thenAccept(result -> log.info("Done: {}", result));
     * </pre>
     */
    public <T> CompletableFuture<T> withLockAsync(Supplier<CompletableFuture<T>> asyncAction) {
        return tryLockAsync().thenCompose(acquired -> {
            if (Boolean.FALSE.equals(acquired)) return CompletableFuture.failedFuture(
                new LockNotAcquiredException("Could not acquire lock: " + lockKey));
            return asyncAction.get().whenComplete((r, ex) -> unlock());
        });
    }

    // -------------------------------------------------------------------------

    public boolean isLocked() { return ops.exists(lockKey); }

    public String lockKey() { return lockKey; }

    public static final class LockNotAcquiredException extends RuntimeException {
        private static final long serialVersionUID = 3882280227581798494L;

        public LockNotAcquiredException(String msg) { super(msg); }
    }
}
