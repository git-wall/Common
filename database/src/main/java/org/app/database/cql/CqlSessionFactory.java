package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.Row;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lifecycle manager for {@link CqlSession}.
 * <p>
 * {@link CqlSession} is thread-safe and expensive to create — create ONE instance
 * per application and reuse it everywhere. This factory handles construction,
 * configuration, and graceful shutdown.
 * <p>
 * Works identically for both <b>Cassandra</b> and <b>ScyllaDB</b>.
 * ScyllaDB shard-aware routing is enabled automatically by the driver
 * when it detects ScyllaDB nodes.
 *
 * <pre>
 * CqlSessionFactory factory = CqlSessionFactory.of(CqlConfig.builder()
 *     .contactPoints(List.of("127.0.0.1:9042"))
 *     .localDatacenter("datacenter1")
 *     .keyspace("myapp")
 *     .build());
 *
 * CqlSession session = factory.session();
 * // pass session to CqlOps, CqlAsync, CqlSchema, etc.
 *
 * factory.close(); // on shutdown
 * </pre>
 */
public final class CqlSessionFactory implements Closeable {

    private final CqlSession session;
    private final CqlConfig  config;

    private CqlSessionFactory(CqlConfig cfg) {
        this.config  = cfg;
        this.session = buildSession(cfg);
    }

    public static CqlSessionFactory of(CqlConfig config) {
        return new CqlSessionFactory(config);
    }

    /** Returns the shared thread-safe session. Never close this manually. */
    public CqlSession session()  { return session; }
    public CqlConfig  config()   { return config; }

    // -------------------------------------------------------------------------
    // Health check
    // -------------------------------------------------------------------------

    /**
     * Verify connectivity by querying system.local.
     * Returns datacenter name if alive, throws otherwise.
     */
    public String ping() {
        Row row = session.execute("SELECT data_center FROM system.local").one();
        if (row == null) throw new CqlException("No response from system.local");
        return row.getString("data_center");
    }

    public boolean isHealthy() {
        try { ping(); return true; }
        catch (Exception e) { return false; }
    }

    // -------------------------------------------------------------------------
    // Build CqlSession
    // -------------------------------------------------------------------------

    private static CqlSession buildSession(CqlConfig cfg) {

        // Parse "host:port" contact points
        List<InetSocketAddress> addresses = cfg.getContactPoints().stream()
            .map(cp -> {
                String[] parts = cp.split(":");
                String host = parts[0].trim();
                int    port = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 9042;
                return InetSocketAddress.createUnresolved(host, port);
            })
            .collect(Collectors.toList());

        // Driver config — programmatic (no application.conf needed)
        DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
            // Timeouts
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT,
                          cfg.getRequestTimeout())
            .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT,
                          cfg.getConnectTimeout())
            .withDuration(DefaultDriverOption.CONTROL_CONNECTION_TIMEOUT,
                          cfg.getMetadataTimeout())
            // Consistency
            .withString(DefaultDriverOption.REQUEST_CONSISTENCY,
                        cfg.getConsistencyLevel())
            .withString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY,
                        cfg.getSerialConsistencyLevel())
            // Page size
            .withInt(DefaultDriverOption.REQUEST_PAGE_SIZE,
                     cfg.getDefaultPageSize())
            // Connection pool
            .withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE,
                     cfg.getLocalPoolSize())
            .withInt(DefaultDriverOption.CONNECTION_POOL_REMOTE_SIZE,
                     cfg.getRemotePoolSize())
            // Reconnection policy — exponential backoff
            .withString(DefaultDriverOption.RECONNECTION_POLICY_CLASS,
                        "ExponentialReconnectionPolicy")
            .withDuration(DefaultDriverOption.RECONNECTION_BASE_DELAY,
                          cfg.getBaseReconnectDelay())
            .withDuration(DefaultDriverOption.RECONNECTION_MAX_DELAY,
                          cfg.getMaxReconnectDelay())
            // Retry policy
            .withString(DefaultDriverOption.RETRY_POLICY_CLASS,
                        "DefaultRetryPolicy")
            // Speculative execution — disabled by default, safe to enable for read-heavy
            .withString(DefaultDriverOption.SPECULATIVE_EXECUTION_POLICY_CLASS,
                        "NoSpeculativeExecutionPolicy")
            .build();

        CqlSessionBuilder builder = CqlSession.builder()
            .addContactPoints(addresses)
            .withLocalDatacenter(cfg.getLocalDatacenter())
            .withConfigLoader(loader);

        if (cfg.getUsername() != null && cfg.getPassword() != null) {
            builder.withAuthCredentials(cfg.getUsername(), cfg.getPassword());
        }
        if (cfg.getKeyspace() != null) {
            builder.withKeyspace(cfg.getKeyspace());
        }
        if (cfg.isSslEnabled()) {
            builder.withSslContext(buildSslContext());
        }

        return builder.build();
    }

    private static javax.net.ssl.SSLContext buildSslContext() {
        try {
            // Default JVM trust store — customize for mutual TLS
            javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
            ctx.init(null, null, null);
            return ctx;
        } catch (Exception e) {
            throw new CqlException("Failed to build SSL context", e);
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        if (!session.isClosed()) {
            session.close();
        }
    }

    // -------------------------------------------------------------------------

    public static final class CqlException extends RuntimeException {
        public CqlException(String msg)                  { super(msg); }
        public CqlException(String msg, Throwable cause) { super(msg, cause); }
    }
}
