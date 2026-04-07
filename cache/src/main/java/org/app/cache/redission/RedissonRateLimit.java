package org.app.cache.redission;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.redisson.api.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed rate limiter via Redisson {@link RRateLimiter}.
 * <p>
 * Limits are enforced globally across all JVM instances — perfect for
 * API throttling, external service call limits, and abuse prevention.
 *
 * <pre>{@code
 * RedissonRateLimit rl = RedissonRateLimit.of(factory);
 *
 * // Allow 100 requests per minute globally
 * rl.define("api:global", 100, Duration.ofMinutes(1));
 *
 * // Per-user rate limit: 10 requests per second
 * rl.define("api:user:" + userId, 10, Duration.ofSeconds(1));
 *
 * // Check and consume (blocks if limit exceeded)
 * rl.acquire("api:global");
 *
 * // Non-blocking check — returns false if rate exceeded
 * boolean allowed = rl.tryAcquire("api:global");
 *
 * // Acquire with timeout
 * boolean allowed = rl.tryAcquire("api:global", Duration.ofMillis(500));
 *
 * // Wrap action — throws RateLimitExceededException if limit exceeded
 * rl.withRateLimit("api:user:alice", () -> callExternalApi());
 *
 * // Async
 * rl.withRateLimitAsync("api:global", () -> CompletableFuture.supplyAsync(() -> fetch()));
 *
 * // Check remaining tokens without consuming
 * RateLimitInfo info = rl.info("api:global");
 * System.out.println("Available: " + info.availablePermits());
 * }</pre>
 */
public final class RedissonRateLimit {

    private final RedissonClient client;

    private RedissonRateLimit(RedissonClient client) {
        this.client = client;
    }

    public static RedissonRateLimit of(RedissonFactory factory) {
        return new RedissonRateLimit(factory.client());
    }

    // =========================================================================
    // Define / initialize
    // =========================================================================

    /**
     * Define a rate limiter: {@code maxRequests} per {@code interval}.
     * Idempotent — safe to call on every startup.
     * Uses OVERALL mode: rate is shared globally across all clients.
     *
     * @param key         unique rate limiter key in Redis
     * @param maxRequests max number of permits per interval
     * @param interval    rolling window duration
     */
    public void define(String key, long maxRequests, Duration interval) {
        RRateLimiter limiter = client.getRateLimiter(key);
        limiter.trySetRate(RateType.OVERALL, maxRequests, interval.toMillis(), RateIntervalUnit.MILLISECONDS);
    }

    /**
     * Define a per-client rate limiter (each Redisson instance gets its own quota).
     * Use for per-instance limits (e.g. each service instance allowed N req/s).
     */
    public void definePerClient(String key, long maxRequests, Duration interval) {
        RRateLimiter limiter = client.getRateLimiter(key);
        limiter.trySetRate(RateType.PER_CLIENT, maxRequests, interval.toMillis(), RateIntervalUnit.MILLISECONDS);
    }

    // =========================================================================
    // Acquire — blocking
    // =========================================================================

    /**
     * Consume 1 permit. Blocks until a permit is available.
     * {@link #define} must be called first.
     */
    public void acquire(String key) {
        client.getRateLimiter(key).acquire();
    }

    /** Consume {@code permits} at once. Blocks until all permits available. */
    public void acquire(String key, long permits) {
        client.getRateLimiter(key).acquire(permits);
    }

    // =========================================================================
    // TryAcquire — non-blocking
    // =========================================================================

    /**
     * Try to consume 1 permit immediately.
     * @return true if permit granted, false if rate limit exceeded
     */
    public boolean tryAcquire(String key) {
        return client.getRateLimiter(key).tryAcquire();
    }

    public boolean tryAcquire(String key, long permits) {
        return client.getRateLimiter(key).tryAcquire(permits);
    }

    /**
     * Try to acquire within timeout.
     * @return true if permit granted within timeout, false otherwise
     */
    public boolean tryAcquire(String key, Duration timeout) {
        return client.getRateLimiter(key).tryAcquire(1, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean tryAcquire(String key, long permits, Duration timeout) {
        return client.getRateLimiter(key).tryAcquire(permits, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    // =========================================================================
    // Wrapper — run action only if within rate limit
    // =========================================================================

    /**
     * Run action if rate limit allows. Acquires immediately (non-blocking).
     * @throws RateLimitExceededException if limit is exceeded
     */
    public void withRateLimit(String key, Runnable action) {
        if (!tryAcquire(key)) throw new RateLimitExceededException("Rate limit exceeded: " + key);
        action.run();
    }

    public <T> T withRateLimit(String key, Supplier<T> action) {
        if (!tryAcquire(key)) throw new RateLimitExceededException("Rate limit exceeded: " + key);
        return action.get();
    }

    /** Block until permit available, then run action. */
    public void withRateLimitBlocking(String key, Runnable action) {
        acquire(key);
        action.run();
    }

    public <T> T withRateLimitBlocking(String key, Supplier<T> action) {
        acquire(key);
        return action.get();
    }

    /** Try acquire with timeout, then run. */
    public void withRateLimit(String key, Duration timeout, Runnable action) {
        if (!tryAcquire(key, timeout)) throw new RateLimitExceededException("Rate limit exceeded: " + key);
        action.run();
    }

    // =========================================================================
    // Async
    // =========================================================================

    public CompletableFuture<Boolean> tryAcquireAsync(String key) {
        return client.getRateLimiter(key).tryAcquireAsync().toCompletableFuture();
    }

    public <T> CompletableFuture<T> withRateLimitAsync(String key, Supplier<CompletableFuture<T>> asyncAction) {
        return tryAcquireAsync(key).thenCompose(acquired -> {
            if (!acquired) return CompletableFuture.failedFuture(
                new RateLimitExceededException("Rate limit exceeded: " + key));
            return asyncAction.get();
        });
    }

    // =========================================================================
    // Introspection
    // =========================================================================

    public RateLimitInfo info(String key) {
        RRateLimiter limiter = client.getRateLimiter(key);
        RateLimiterConfig cfg = limiter.getConfig();
        long available = limiter.availablePermits();
        return new RateLimitInfo(cfg.getRate(), cfg.getRateInterval(), available);
    }

    public long availablePermits(String key) {
        return client.getRateLimiter(key).availablePermits();
    }

    /** Reset the limiter (clear consumed tokens). */
    public void reset(String key) {
        client.getRateLimiter(key).delete();
    }

    /** Get raw RRateLimiter for advanced usage. */
    public RRateLimiter rrateLimiter(String key) { return client.getRateLimiter(key); }

    // =========================================================================
    // Types
    // =========================================================================

    @AllArgsConstructor
    @NoArgsConstructor
    public static class RateLimitInfo {
        long maxRate; long intervalMs; long availablePermits;
        public double requestsPerSecond() { return (double) maxRate / intervalMs * 1000; }

        @Override public String toString() {
            return String.format("RateLimitInfo[max=%d per %dms, available=%d, rps=%.1f]",
                maxRate, intervalMs, availablePermits, requestsPerSecond());
        }
    }

    public static final class RateLimitExceededException extends RuntimeException {
        private static final long serialVersionUID = 1964022984041937978L;

        public RateLimitExceededException(String msg) { super(msg); }
    }
}
