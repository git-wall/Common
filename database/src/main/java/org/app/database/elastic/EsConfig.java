package org.app.database.elastic;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Immutable Elasticsearch 7.x connection configuration.
 *
 * <pre>
 * EsConfig config = EsConfig.builder()
 *     .hosts(List.of("localhost:9200", "node2:9200"))
 *     .username("elastic").password("changeme")
 *     .build();
 * </pre>
 */
@Getter
@Builder
public class EsConfig {

    /** ES nodes in "host:port" format. */
    @Builder.Default
    List<String> hosts = List.of("localhost:9200");

    @Builder.Default String scheme   = "http";
    @Builder.Default String username = null;
    @Builder.Default String password = null;

    // ---- Connection pool ----
    @Builder.Default int connectTimeoutMs    = 5_000;
    @Builder.Default int socketTimeoutMs     = 30_000;
    @Builder.Default int connectionRequestMs = 1_000;
    @Builder.Default int maxConnTotal        = 200;
    @Builder.Default int maxConnPerRoute     = 100;

    // ---- Retry ----
    @Builder.Default int maxRetryMs = 10_000;   // org.elasticsearch.client.sniff / retry timeout
}
