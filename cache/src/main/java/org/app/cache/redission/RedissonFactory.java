package org.app.cache.redission;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;

import java.io.Closeable;

/**
 * Lifecycle manager for {@link RedissonClient}.
 * Create once at startup, close on shutdown.
 *
 * <pre>{@code
 * RedissonFactory factory = RedissonFactory.of(RedissonConfig.standalone()
 *     .address("redis://localhost:6379")
 *     .password("secret")
 *     .build());
 *
 * RedissonClient client = factory.client();
 * // ...
 * factory.close();
 * }</pre>
 */
public final class RedissonFactory implements Closeable {

    private final RedissonClient client;
    private final RedissonConfig appConfig;

    private RedissonFactory(RedissonConfig appCfg) {
        this.appConfig = appCfg;
        this.client    = Redisson.create(buildConfig(appCfg));
    }

    public static RedissonFactory of(RedissonConfig config) {
        return new RedissonFactory(config);
    }

    public RedissonClient client()      { return client; }
    public RedissonConfig appConfig()   { return appConfig; }

    // -------------------------------------------------------------------------
    // Ping
    // -------------------------------------------------------------------------

    public boolean ping() {
        try {
            client.getBucket("__ping__").isExists();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Build org.redisson.config.Config
    // -------------------------------------------------------------------------

    private static Config buildConfig(RedissonConfig cfg) {
        Config c = new Config();
        c.setThreads(cfg.getThreads());
        c.setNettyThreads(cfg.getNettyThreads());

        switch (cfg.getMode()) {
            case STANDALONE  : configureStandalone(c, cfg); break;
            case CLUSTER     : configureCluster(c, cfg); break;
            case SENTINEL    : configureSentinel(c, cfg); break;
            case REPLICATED  : configureReplicated(c, cfg);
        }
        return c;
    }

    private static void configureStandalone(Config c, RedissonConfig cfg) {
        SingleServerConfig s = c.useSingleServer()
            .setAddress(cfg.getAddress())
            .setDatabase(cfg.getDatabase())
            .setConnectionPoolSize(cfg.getConnectionPoolSize())
            .setConnectionMinimumIdleSize(cfg.getConnectionMinimumIdle())
            .setConnectTimeout(cfg.getConnectTimeoutMs())
            .setIdleConnectionTimeout(cfg.getIdleConnectionTimeoutMs())
            .setPingConnectionInterval(cfg.getPingConnectionIntervalMs())
            .setRetryAttempts(cfg.getRetryAttempts())
            .setRetryInterval(cfg.getRetryIntervalMs())
            .setTimeout(cfg.getResponseTimeoutMs());
        if (cfg.getPassword() != null) s.setPassword(cfg.getPassword());
        if (cfg.getUsername() != null) s.setUsername(cfg.getUsername());
    }

    private static void configureCluster(Config c, RedissonConfig cfg) {
        ClusterServersConfig s = c.useClusterServers()
            .addNodeAddress(cfg.getNodeAddresses().toArray(new String[0]))
            .setConnectTimeout(cfg.getConnectTimeoutMs())
            .setIdleConnectionTimeout(cfg.getIdleConnectionTimeoutMs())
            .setPingConnectionInterval(cfg.getPingConnectionIntervalMs())
            .setRetryAttempts(cfg.getRetryAttempts())
            .setRetryInterval(cfg.getRetryIntervalMs())
            .setTimeout(cfg.getResponseTimeoutMs())
            .setMasterConnectionPoolSize(cfg.getConnectionPoolSize())
            .setMasterConnectionMinimumIdleSize(cfg.getConnectionMinimumIdle());
        if (cfg.getPassword() != null) s.setPassword(cfg.getPassword());
        if (cfg.getUsername() != null) s.setUsername(cfg.getUsername());
    }

    private static void configureSentinel(Config c, RedissonConfig cfg) {
        SentinelServersConfig s = c.useSentinelServers()
            .setMasterName(cfg.getMasterName())
            .addSentinelAddress(cfg.getSentinelAddresses().toArray(new String[0]))
            .setDatabase(cfg.getDatabase())
            .setConnectTimeout(cfg.getConnectTimeoutMs())
            .setIdleConnectionTimeout(cfg.getIdleConnectionTimeoutMs())
            .setPingConnectionInterval(cfg.getPingConnectionIntervalMs())
            .setRetryAttempts(cfg.getRetryAttempts())
            .setRetryInterval(cfg.getRetryIntervalMs())
            .setTimeout(cfg.getResponseTimeoutMs())
            .setMasterConnectionPoolSize(cfg.getConnectionPoolSize())
            .setMasterConnectionMinimumIdleSize(cfg.getConnectionMinimumIdle());
        if (cfg.getPassword() != null) s.setPassword(cfg.getPassword());
        if (cfg.getUsername() != null) s.setUsername(cfg.getUsername());
    }

    private static void configureReplicated(Config c, RedissonConfig cfg) {
        ReplicatedServersConfig s = c.useReplicatedServers()
            .addNodeAddress(cfg.getNodeAddresses().toArray(new String[0]))
            .setDatabase(cfg.getDatabase())
            .setConnectTimeout(cfg.getConnectTimeoutMs())
            .setIdleConnectionTimeout(cfg.getIdleConnectionTimeoutMs())
            .setPingConnectionInterval(cfg.getPingConnectionIntervalMs())
            .setRetryAttempts(cfg.getRetryAttempts())
            .setRetryInterval(cfg.getRetryIntervalMs())
            .setTimeout(cfg.getResponseTimeoutMs())
            .setMasterConnectionPoolSize(cfg.getConnectionPoolSize())
            .setMasterConnectionMinimumIdleSize(cfg.getConnectionMinimumIdle());
        if (cfg.getPassword() != null) s.setPassword(cfg.getPassword());
        if (cfg.getUsername() != null) s.setUsername(cfg.getUsername());
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        if (!client.isShutdown()) {
            client.shutdown(
                appConfig.getShutdownTimeout().toMillis(),
                appConfig.getShutdownTimeout().toMillis() * 2,
                java.util.concurrent.TimeUnit.MILLISECONDS
            );
        }
    }
}
