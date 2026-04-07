package org.app.cache.lettuce;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

/**
 * Immutable Lettuce connection configuration.
 * Supports Standalone, Cluster, and Sentinel modes.
 *
 * <pre>{@code
 * // Standalone
 * LettuceConfig cfg = LettuceConfig.standalone()
 *     .host("localhost").port(6379)
 *     .password("secret")
 *     .database(1)
 *     .build();
 *
 * // Cluster
 * LettuceConfig cfg = LettuceConfig.cluster()
 *     .clusterNodes(List.of("node1:7001","node2:7002","node3:7003"))
 *     .build();
 *
 * // Sentinel
 * LettuceConfig cfg = LettuceConfig.sentinel()
 *     .sentinelMasterId("mymaster")
 *     .sentinelNodes(List.of("s1:26379","s2:26379"))
 *     .build();
 * }</pre>
 */
@Getter
@Builder(builderMethodName = "_builder")
public class LettuceConfig {

    public enum Mode { STANDALONE, CLUSTER, SENTINEL }

    // ---- Mode ----
    @Builder.Default Mode   mode     = Mode.STANDALONE;

    // ---- Standalone / Sentinel auth ----
    @Builder.Default String host     = "localhost";
    @Builder.Default int    port     = 6379;
    @Builder.Default String password = null;
    @Builder.Default String username = null;   // ACL (Redis 6+)
    @Builder.Default int    database = 0;
    @Builder.Default boolean ssl     = false;

    // ---- Cluster ----
    @Builder.Default List<String> clusterNodes = List.of();

    // ---- Sentinel ----
    @Builder.Default String       sentinelMasterId = "mymaster";
    @Builder.Default List<String> sentinelNodes    = List.of();

    // ---- Timeouts ----
    @Builder.Default Duration commandTimeout  = Duration.ofSeconds(5);
    @Builder.Default Duration connectTimeout  = Duration.ofSeconds(5);
    @Builder.Default Duration shutdownTimeout = Duration.ofSeconds(2);

    // ---- Connection pool (for pooled factory) ----
    @Builder.Default int poolMaxTotal = 32;
    @Builder.Default int poolMaxIdle  = 8;
    @Builder.Default int poolMinIdle  = 2;

    // ---- Compression ----
    @Builder.Default boolean compressionEnabled        = true;
    @Builder.Default int     compressionThresholdBytes = 512;

    // ---- Static factory methods for ergonomic builder ----

    public static LettuceConfigBuilder standalone() {
        return _builder().mode(Mode.STANDALONE);
    }

    public static LettuceConfigBuilder cluster() {
        return _builder().mode(Mode.CLUSTER);
    }

    public static LettuceConfigBuilder sentinel() {
        return _builder().mode(Mode.SENTINEL);
    }
}
