package org.app.cache.cache2lv;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * Immutable configuration for a {@link TieredCache} instance.
 *
 * <pre>
 * CacheConfig config = CacheConfig.builder()
 *     .ttl(Duration.ofMinutes(5))
 *     .refreshLeadTime(Duration.ofSeconds(30))   // prefetch 30s before expiry
 *     .localMaxSize(1000)
 *     .l2Enabled(true)
 *     .l2Ttl(Duration.ofMinutes(30))
 *     .loaderRetries(2)
 *     .loaderRetryDelay(Duration.ofMillis(200))
 *     .allowStaleOnError(true)                   // return stale if loader fails
 *     .build();
 * </pre>
 */
@Getter
@Builder
public class CacheConfig {

    // ---- L1 local config ------------------------------------------------

    /** Hard TTL for L1. After this, entry is evicted. */
    @Builder.Default
    Duration ttl = Duration.ofMinutes(5);

    /**
     * How early before {@code ttl} expiry to trigger a background refresh.
     * E.g. ttl=5min, refreshLeadTime=30s → refresh fires at 4m30s.
     * Set to ZERO to disable proactive refresh.
     */
    @Builder.Default
    Duration refreshLeadTime = Duration.ofSeconds(30);

    /** Max number of entries in L1 per cache instance. */
    @Builder.Default
    int localMaxSize = 1_000;

    /** How often the background cleanup/refresh thread wakes up (ms). */
    @Builder.Default
    Duration maintenanceInterval = Duration.ofSeconds(10);

    // ---- L2 distributed config ------------------------------------------

    /** Enable L2 distributed cache tier. */
    @Builder.Default
    boolean l2Enabled = true;

    /**
     * TTL for values stored in L2. Should be >= L1 ttl to allow
     * other instances to warm up from L2 before hitting loader.
     */
    @Builder.Default
    Duration l2Ttl = Duration.ofMinutes(30);

    // ---- Loader / fallback config ----------------------------------------

    /** Number of retry attempts on loader failure (0 = no retry). */
    @Builder.Default
    int loaderRetries = 2;

    /** Delay between loader retries. */
    @Builder.Default
    Duration loaderRetryDelay = Duration.ofMillis(200);

    /**
     * If true, serve stale (expired) L1 value when loader AND L2 both fail,
     * rather than throwing an exception. Useful for resilience.
     */
    @Builder.Default
    boolean allowStaleOnError = true;

    /**
     * How long to keep a stale entry available for emergency fallback
     * beyond the normal TTL (separate from hard eviction).
     * Stale fallback window = ttl + staleGracePeriod.
     */
    @Builder.Default
    Duration staleGracePeriod = Duration.ofMinutes(10);

    // ---- Anti-stampede --------------------------------------------------

    /**
     * When multiple threads miss the same key simultaneously, only one
     * calls the loader; others wait up to this duration.
     * After timeout they either get stale or throw.
     */
    @Builder.Default
    Duration stampedeLockTimeout = Duration.ofSeconds(5);
}
