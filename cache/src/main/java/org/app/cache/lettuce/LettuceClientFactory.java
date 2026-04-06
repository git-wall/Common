package org.app.cache.lettuce;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.Getter;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lifecycle manager for Lettuce Redis connections.
 * <p>
 * Lettuce connections are thread-safe — one connection is typically enough
 * for a whole application. This factory also supports an optional connection
 * pool (via Apache Commons Pool2) for use cases that need multiple concurrent
 * blocking connections.
 * <p>
 * <b>Design note on Cluster vs Standalone:</b><br>
 * {@link StatefulRedisConnection} and {@link StatefulRedisClusterConnection} share
 * the common ancestor {@code StatefulConnection} but are NOT in a subtype relationship.
 * Their sync/async command interfaces ({@link RedisCommands} /
 * {@link RedisClusterCommands}) both extend {@code BaseRedisCommands} but also
 * diverge for cluster-specific ops. To avoid unsafe casting, this factory exposes
 * <em>commands</em> (via the widest common interface) rather than raw connections.
 *
 * <pre>{@code
 * LettuceClientFactory factory = LettuceClientFactory.of(LettuceConfig.standalone().build());
 *
 * // Sync string commands (works for standalone AND cluster)
 * RedisClusterCommands&lt;String,String&gt; cmds = factory.sync();
 *
 * // Async string commands
 * RedisClusterAsyncCommands&lt;String,String&gt; async = factory.async();
 *
 * // Sync byte-array commands (LZ4 compression path)
 * RedisClusterCommands&lt;byte[],byte[]&gt; bin = factory.binarySync();
 *
 * factory.close(); // on shutdown
 * }</pre>
 */
public final class LettuceClientFactory implements Closeable {

    @Getter
    private final LettuceConfig config;

    // Standalone
    private RedisClient standaloneClient;
    private StatefulRedisConnection<String, String> stringConn;
    private StatefulRedisConnection<byte[], byte[]> binaryConn;

    // Cluster
    private RedisClusterClient clusterClient;
    private StatefulRedisClusterConnection<String, String> clusterConn;
    private StatefulRedisClusterConnection<byte[], byte[]> clusterBinaryConn;

    // Optional pool
    private GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    private LettuceClientFactory(LettuceConfig cfg) {
        this.config = cfg;
        switch (cfg.getMode()) {
            case STANDALONE:
                initStandalone(cfg);
                break;
            case CLUSTER:
                initCluster(cfg);
                break;
            case SENTINEL:
                initSentinel(cfg);
        }
    }

    public static LettuceClientFactory of(LettuceConfig config) {
        return new LettuceClientFactory(config);
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    private void initStandalone(LettuceConfig cfg) {
        RedisURI uri = buildUri(cfg.getHost(), cfg.getPort(), cfg);
        standaloneClient = RedisClient.create(uri);
        standaloneClient.setOptions(ClientOptions.builder()
            .timeoutOptions(TimeoutOptions.enabled(cfg.getCommandTimeout()))
            .socketOptions(SocketOptions.builder()
                .connectTimeout(cfg.getConnectTimeout())
                .build())
            .autoReconnect(true)
            .build());
        stringConn = standaloneClient.connect();
        binaryConn = standaloneClient.connect(ByteArrayCodec.INSTANCE);
    }

    private void initCluster(LettuceConfig cfg) {
        List<RedisURI> uris = cfg.getClusterNodes().stream()
            .map(n -> {
                String[] p = n.split(":");
                return buildUri(p[0], Integer.parseInt(p[1]), cfg);
            })
            .collect(Collectors.toList());

        clusterClient = RedisClusterClient.create(uris);
        clusterClient.setOptions(ClusterClientOptions.builder()
            .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .refreshPeriod(Duration.ofMinutes(1))
                .build())
            .autoReconnect(true)
            .build());
        clusterConn = clusterClient.connect();
        clusterBinaryConn = clusterClient.connect(ByteArrayCodec.INSTANCE);
    }

    private void initSentinel(LettuceConfig cfg) {
        // Build sentinel URI
        RedisURI.Builder b = RedisURI.Builder
            .sentinel(cfg.getSentinelNodes().get(0).split(":")[0],
                Integer.parseInt(cfg.getSentinelNodes().get(0).split(":")[1]),
                cfg.getSentinelMasterId());
        cfg.getSentinelNodes().stream().skip(1).forEach(n -> {
            String[] p = n.split(":");
            b.withSentinel(p[0], Integer.parseInt(p[1]));
        });
        if (cfg.getPassword() != null)
            b.withPassword(cfg.getPassword().toCharArray());
        b.withDatabase(cfg.getDatabase());
        b.withTimeout(cfg.getCommandTimeout());

        standaloneClient = RedisClient.create(b.build());
        stringConn = standaloneClient.connect();
        binaryConn = standaloneClient.connect(ByteArrayCodec.INSTANCE);
    }

    // -------------------------------------------------------------------------
    // Command accessors  (correct types — no unsafe casting)
    // -------------------------------------------------------------------------
    //
    // StatefulRedisConnection      → sync()  returns RedisCommands        (extends RedisClusterCommands)
    // StatefulRedisClusterConnection → sync() returns RedisClusterCommands
    //
    // Both RedisCommands and RedisClusterCommands extend the same
    // RedisClusterCommands<K,V> interface, so we expose that as the widest
    // common type that still gives access to all standard Redis commands.
    // -------------------------------------------------------------------------

    /**
     * Sync string commands — works transparently for both Standalone and Cluster.
     * The returned {@link RedisClusterCommands} is the widest interface shared by
     * both {@code RedisCommands} (standalone) and {@code RedisClusterCommands}
     * (cluster), so call sites need no mode-specific branching.
     */
    public RedisClusterCommands<String, String> sync() {
        return config.getMode() == LettuceConfig.Mode.CLUSTER
            ? clusterConn.sync()
            : stringConn.sync();
    }

    /**
     * Async string commands — works transparently for both Standalone and Cluster.
     * {@link RedisClusterAsyncCommands} is the common ancestor of both
     * {@code RedisAsyncCommands} and {@code RedisClusterAsyncCommands}.
     */
    public RedisClusterAsyncCommands<String, String> async() {
        return config.getMode() == LettuceConfig.Mode.CLUSTER
            ? clusterConn.async()
            : stringConn.async();
    }

    /**
     * Sync byte-array commands — used internally for LZ4-compressed values.
     */
    public RedisClusterCommands<byte[], byte[]> binarySync() {
        return config.getMode() == LettuceConfig.Mode.CLUSTER
            ? clusterBinaryConn.sync()
            : binaryConn.sync();
    }

    /**
     * Async byte-array commands — used internally for LZ4-compressed values.
     */
    public RedisClusterAsyncCommands<byte[], byte[]> binaryAsync() {
        return config.getMode() == LettuceConfig.Mode.CLUSTER
            ? clusterBinaryConn.async()
            : binaryConn.async();
    }

    /**
     * Raw standalone connection — only valid in STANDALONE / SENTINEL mode.
     * Needed for operations that require a {@link StatefulRedisConnection} directly
     * (e.g. pipeline flush via {@code setAutoFlushCommands}).
     */
    public StatefulRedisConnection<String, String> standaloneConnection() {
        if (stringConn == null)
            throw new IllegalStateException("Not in STANDALONE/SENTINEL mode");
        return stringConn;
    }

    /**
     * Raw standalone binary connection — only valid in STANDALONE / SENTINEL mode.
     */
    public StatefulRedisConnection<byte[], byte[]> standaloneBinaryConnection() {
        if (binaryConn == null)
            throw new IllegalStateException("Not in STANDALONE/SENTINEL mode");
        return binaryConn;
    }

    /**
     * Raw cluster connection — only valid in CLUSTER mode.
     */
    public StatefulRedisClusterConnection<String, String> clusterConnection() {
        if (clusterConn == null)
            throw new IllegalStateException("Not in CLUSTER mode");
        return clusterConn;
    }

    /**
     * Raw cluster binary connection — only valid in CLUSTER mode.
     */
    public StatefulRedisClusterConnection<byte[], byte[]> clusterBinaryConnection() {
        if (clusterBinaryConn == null)
            throw new IllegalStateException("Not in CLUSTER mode");
        return clusterBinaryConn;
    }

    /**
     * Create a dedicated Pub/Sub string connection.
     * Pub/Sub requires its own connection (cannot share with command connection).
     * The caller is responsible for closing it.
     */
    public io.lettuce.core.pubsub.StatefulRedisPubSubConnection<String, String> connectPubSub() {
        return config.getMode() == LettuceConfig.Mode.CLUSTER
            ? clusterClient.connectPubSub()
            : standaloneClient.connectPubSub();
    }

    /**
     * Enable connection pool (Commons Pool2).
     * Only for STANDALONE mode. Call once before using pooled connections.
     */
    public void enablePool() {
        if (config.getMode() != LettuceConfig.Mode.STANDALONE)
            throw new IllegalStateException("Pool only supported in STANDALONE mode");
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolCfg =
            new GenericObjectPoolConfig<>();
        poolCfg.setMaxTotal(config.getPoolMaxTotal());
        poolCfg.setMaxIdle(config.getPoolMaxIdle());
        poolCfg.setMinIdle(config.getPoolMinIdle());
        poolCfg.setTestOnBorrow(true);
        pool = ConnectionPoolSupport.createGenericObjectPool(
            () -> standaloneClient.connect(), poolCfg);
    }

    /**
     * Borrow a connection from the pool (must call {@link #enablePool()} first).
     * Use try-with-resources.
     */
    public StatefulRedisConnection<String, String> borrowConnection() {
        if (pool == null)
            throw new IllegalStateException("Pool not enabled — call enablePool() first");
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new LettuceOps.LettuceException("borrow pool connection failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Ping
    // -------------------------------------------------------------------------

    public String ping() {
        return sync().ping();
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        Duration shutdown = config.getShutdownTimeout();
        if (pool != null) pool.close();
        if (stringConn != null) stringConn.close();
        if (binaryConn != null) binaryConn.close();
        if (clusterConn != null) clusterConn.close();
        if (clusterBinaryConn != null) clusterBinaryConn.close();
        if (standaloneClient != null)
            standaloneClient.shutdown(shutdown.toMillis(), shutdown.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (clusterClient != null)
            clusterClient.shutdown(shutdown.toMillis(), shutdown.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // -------------------------------------------------------------------------

    private RedisURI buildUri(String host, int port, LettuceConfig cfg) {
        RedisURI.Builder b = RedisURI.Builder.redis(host, port)
            .withTimeout(cfg.getCommandTimeout())
            .withDatabase(cfg.getDatabase())
            .withSsl(cfg.isSsl());
        if (cfg.getPassword() != null) {
            b.withPassword(cfg.getPassword().toCharArray());
            if (cfg.getUsername() != null)
                b.withAuthentication(cfg.getUsername(), cfg.getPassword().toCharArray());
        }
        return b.build();
    }
}
