package org.app.common.db;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceUtils {

    public static final int STMT_CACHE_SIZE = 25000;
    public static final int STMT_CACHE_LIMIT = 20048;
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_POOL_SIZE = 25000;
    public static final long CONNECTION_TIMEOUT = 20000L;

    public static void appendConfig(HikariDataSource dataSource) {
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", STMT_CACHE_SIZE);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", STMT_CACHE_LIMIT);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        dataSource.addDataSourceProperty("initializationFailFast", true);
    }

    public static DataSource defaultDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(MAX_POOL_SIZE);
        dataSource.setMinimumIdle(MIN_POOL_SIZE);
        dataSource.setIdleTimeout(300000L);
        dataSource.setConnectionTimeout(CONNECTION_TIMEOUT);
        return dataSource;
    }

    public static DataSource dataSource(long cnnTimeOut, long idleTimeOut, String poolName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(idleTimeOut);
        dataSource.setConnectionTimeout(cnnTimeOut);
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    public static DataSource hikariDataSource(long cnnTimeOut, long idleTimeOut, String poolName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(idleTimeOut);
        dataSource.setConnectionTimeout(cnnTimeOut);
        dataSource.setPoolName(poolName);
        appendConfig(dataSource);
        return dataSource;
    }
}
