package org.app.cache.redission;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

/**
 * Immutable Redisson connection configuration supporting 4 deployment modes.
 *
 * <pre>{@code
 * // Standalone
 * RedissonConfig cfg = RedissonConfig.standalone()
 *     .address("redis://localhost:6379")
 *     .password("secret")
 *     .build();
 *
 * // Cluster
 * RedissonConfig cfg = RedissonConfig.cluster()
 *     .nodeAddresses(List.of(
 *         "redis://node1:7001",
 *         "redis://node2:7002",
 *         "redis://node3:7003"))
 *     .build();
 *
 * // Sentinel
 * RedissonConfig cfg = RedissonConfig.sentinel()
 *     .masterName("mymaster")
 *     .sentinelAddresses(List.of(
 *         "redis://s1:26379",
 *         "redis://s2:26379"))
 *     .build();
 *
 * // Replicated (master + replicas, auto failover)
 * RedissonConfig cfg = RedissonConfig.replicated()
 *     .nodeAddresses(List.of(
 *         "redis://master:6379",
 *         "redis://replica1:6380"))
 *     .build();
 * }</pre>
 */
@Getter
@Builder(builderMethodName = "_builder")
public class RedissonConfig {

    public enum Mode { STANDALONE, CLUSTER, SENTINEL, REPLICATED }

    // ---- Mode ----
    @Builder.Default Mode   mode    = Mode.STANDALONE;

    // ---- Standalone ----
    @Builder.Default String address = "redis://localhost:6379";   // or rediss:// for TLS

    // ---- Cluster / Replicated node list ----
    @Builder.Default List<String> nodeAddresses = List.of();

    // ---- Sentinel ----
    @Builder.Default String       masterName        = "mymaster";
    @Builder.Default List<String> sentinelAddresses = List.of();

    // ---- Auth ----
    @Builder.Default String password = null;
    @Builder.Default String username = null;   // ACL (Redis 6+)
    @Builder.Default int    database = 0;

    // ---- Connection pool ----
    @Builder.Default int connectionPoolSize    = 64;
    @Builder.Default int connectionMinimumIdle = 10;

    // ---- Timeouts ----
    @Builder.Default int connectTimeoutMs    = 10_000;
    @Builder.Default int idleConnectionTimeoutMs = 10_000;
    @Builder.Default int pingConnectionIntervalMs = 30_000;
    @Builder.Default int retryAttempts       = 3;
    @Builder.Default int retryIntervalMs     = 1_500;
    @Builder.Default int responseTimeoutMs   = 3_000;
    @Builder.Default Duration shutdownTimeout = Duration.ofSeconds(3);

    // ---- Codec ----
    // Default: Jackson JSON. Override with RedissonFactory.of(cfg, codec).
    // Common choices: JsonJacksonCodec, MsgPackJacksonCodec, Kryo5Codec
    @Builder.Default String codecClass = "org.redisson.codec.JsonJacksonCodec";

    // ---- Threads ----
    @Builder.Default int threads     = 0;  // 0 = 2x CPU cores
    @Builder.Default int nettyThreads = 0; // 0 = 2x CPU cores

    // ---- Static factory methods ----

    public static RedissonConfigBuilder standalone() {
        return _builder().mode(Mode.STANDALONE);
    }

    public static RedissonConfigBuilder cluster() {
        return _builder().mode(Mode.CLUSTER);
    }

    public static RedissonConfigBuilder sentinel() {
        return _builder().mode(Mode.SENTINEL);
    }

    public static RedissonConfigBuilder replicated() {
        return _builder().mode(Mode.REPLICATED);
    }
}
