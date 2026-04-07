package org.app.database.cql;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

/**
 * Immutable connection configuration for Cassandra and ScyllaDB.
 * <p>
 * Both databases use the same CQL protocol and Java driver — this config
 * works identically for both. The only difference is the datacenter name
 * (check {@code nodetool status} or {@code SELECT data_center FROM system.local}).
 *
 * <pre>{@code
 * // Cassandra
 * CqlConfig cfg = CqlConfig.builder()
 *     .contactPoints(List.of("127.0.0.1:9042"))
 *     .localDatacenter("datacenter1")
 *     .keyspace("myapp")
 *     .build();
 *
 * // ScyllaDB
 * CqlConfig cfg = CqlConfig.builder()
 *     .contactPoints(List.of("node1:9042", "node2:9042", "node3:9042"))
 *     .localDatacenter("datacenter1")   // from: SELECT data_center FROM system.local
 *     .keyspace("myapp")
 *     .username("cassandra")
 *     .password("cassandra")
 *     .build();
 *
 * // Production multi-DC
 * CqlConfig cfg = CqlConfig.builder()
 *     .contactPoints(List.of("10.0.1.1:9042", "10.0.1.2:9042"))
 *     .localDatacenter("us-east")
 *     .keyspace("myapp")
 *     .consistencyLevel("LOCAL_QUORUM")
 *     .serialConsistencyLevel("LOCAL_SERIAL")
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class CqlConfig {

    // ── Contact points ───────────────────────────────────────────────────────
    /** Seed nodes in "host:port" format. Driver discovers the rest via gossip. */
    @Builder.Default
    List<String> contactPoints = List.of("127.0.0.1:9042");

    /**
     * Local datacenter for load balancing policy.
     * Driver only sends requests to nodes in this DC.
     * Find with: SELECT data_center FROM system.local
     */
    @Builder.Default
    String localDatacenter = "datacenter1";

    // ── Auth ─────────────────────────────────────────────────────────────────
    @Builder.Default String username = null;
    @Builder.Default String password = null;

    // ── Keyspace ─────────────────────────────────────────────────────────────
    /** Default keyspace. Can be overridden per query. */
    @Builder.Default String keyspace = null;

    // ── Consistency ──────────────────────────────────────────────────────────
    /**
     * Default consistency level.
     * Values: ANY, ONE, TWO, THREE, QUORUM, ALL, LOCAL_ONE, LOCAL_QUORUM,
     *         EACH_QUORUM, SERIAL, LOCAL_SERIAL
     * Recommended: LOCAL_QUORUM for production multi-DC
     */
    @Builder.Default String consistencyLevel       = "LOCAL_QUORUM";
    @Builder.Default String serialConsistencyLevel = "LOCAL_SERIAL"; // for LWT (IF NOT EXISTS etc)

    // ── Timeouts ─────────────────────────────────────────────────────────────
    @Builder.Default Duration requestTimeout      = Duration.ofSeconds(10);
    @Builder.Default Duration connectTimeout      = Duration.ofSeconds(10);
    @Builder.Default Duration metadataTimeout     = Duration.ofSeconds(20);
    @Builder.Default Duration shutdownTimeout     = Duration.ofSeconds(10);

    // ── Connection pool ───────────────────────────────────────────────────────
    @Builder.Default int localPoolSize  = 4;   // connections per local node
    @Builder.Default int remotePoolSize = 2;   // connections per remote node

    // ── Reconnection ─────────────────────────────────────────────────────────
    @Builder.Default Duration baseReconnectDelay = Duration.ofSeconds(1);
    @Builder.Default Duration maxReconnectDelay  = Duration.ofSeconds(60);

    // ── Fetch size (paging) ───────────────────────────────────────────────────
    /** Number of rows fetched per page when iterating large result sets. */
    @Builder.Default int defaultPageSize = 1000;

    // ── SSL ──────────────────────────────────────────────────────────────────
    @Builder.Default boolean sslEnabled = false;

    // ── Schema management ────────────────────────────────────────────────────
    /** Auto-create keyspace and tables if missing (dev convenience). */
    @Builder.Default boolean schemaAutoCreate = false;
}
