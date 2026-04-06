package org.app.cache.jedis;


import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;

/**
 * Manages a single {@link JedisPool} from a {@link RedisConfig}.
 * <pre>{@code
 * RedisClientFactory factory = RedisClientFactory.of(config);
 * try (Jedis jedis = factory.getResource()) { ... }
 * factory.close(); // on shutdown
 * }</pre>
 */
@Getter
public final class RedisClientFactory implements Closeable {

    private final JedisPool pool;
    private final RedisConfig config;

    private RedisClientFactory(RedisConfig config) {
        this.config = config;
        JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(config.getMaxTotal());
        poolCfg.setMaxIdle(config.getMaxIdle());
        poolCfg.setMinIdle(config.getMinIdle());
        poolCfg.setTestOnBorrow(config.isTestOnBorrow());
        poolCfg.setTestWhileIdle(config.isTestWhileIdle());

        if (config.getPassword() != null) {
            this.pool = new JedisPool(poolCfg,
                config.getHost(), config.getPort(),
                config.getConnectionTimeout(), config.getSocketTimeout(),
                config.getPassword(), config.getDatabase(), null);
        } else {
            this.pool = new JedisPool(poolCfg,
                config.getHost(), config.getPort(),
                config.getConnectionTimeout(), config.getSocketTimeout(),
                null, config.getDatabase(), null);
        }
    }

    /** Create a factory (and underlying pool) from a config. */
    public static RedisClientFactory of(RedisConfig config) {
        return new RedisClientFactory(config);
    }

    /** Borrow a Jedis connection from the pool. Must be closed (try-with-resources). */
    public Jedis getResource() {
        return pool.getResource();
    }

    /** Returns pool stats as a readable string for monitoring / logging. */
    public String poolStats() {
        return String.format("Pool[active=%d, idle=%d, waiters=%d]",
            pool.getNumActive(), pool.getNumIdle(), pool.getNumWaiters());
    }

    @Override
    public void close() {
        pool.close();
    }
}
