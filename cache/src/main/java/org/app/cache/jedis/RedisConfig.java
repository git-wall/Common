package org.app.cache.jedis;

import lombok.Builder;
import lombok.Getter;

/**
 * Immutable Redis connection config.
 * <pre>{@code
 * RedisConfig config = RedisConfig.builder()
 *     .host("localhost").port(6379)
 *     .password("secret")
 *     .database(1)
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class RedisConfig {

    @Builder.Default
    private final String host = "localhost";
    @Builder.Default
    private final int port = 6379;
    @Builder.Default
    private final String password = null;
    @Builder.Default
    private final int database = 0;
    @Builder.Default
    private final int connectionTimeout = 2_000;  // ms
    @Builder.Default
    private final int socketTimeout = 2_000;  // ms
    @Builder.Default
    private final int maxTotal = 32;
    @Builder.Default
    private final int maxIdle = 8;
    @Builder.Default
    private final int minIdle = 2;
    @Builder.Default
    private final boolean testOnBorrow = true;
    @Builder.Default
    private final boolean testWhileIdle = true;
    /**
     * Compress values whose raw byte length >= this threshold. 0 = always compress.
     */
    @Builder.Default
    private final int compressionThresholdBytes = 512;
    /**
     * Whether to enable LZ4 compression globally.
     */
    @Builder.Default
    private final boolean compressionEnabled = true;
}
