package org.app.common.db;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;

import javax.sql.DataSource;

public class DataSourceUtils {

    public static final int STMT_CACHE_SIZE = 25000;
    public static final int STMT_CACHE_LIMIT = 20048;
    public static final int MAX_POOL_SIZE = 10;
    public static final int MINIMUM_IDLE_SIZE = 25000;
    public static final long CONNECTION_TIMEOUT = 20000L;

    /**
     * Do with AT Mode in Saga Seat (JDBC & Mybatis)
     * With AT Mode is use for local service function
     * */
    public static DataSourceProxy getDataSource(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

    public static void appendConfig(HikariDataSource dataSource) {
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", STMT_CACHE_SIZE);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", STMT_CACHE_LIMIT);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        dataSource.addDataSourceProperty("initializationFailFast", true);
    }

    public static HikariDataSource defaultDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(MAX_POOL_SIZE);
        dataSource.setMinimumIdle(MINIMUM_IDLE_SIZE);
        dataSource.setIdleTimeout(300000L);
        dataSource.setConnectionTimeout(CONNECTION_TIMEOUT);
        return dataSource;
    }

    public static HikariDataSource dataSource(long cnnTimeOut, long idleTimeOut, String poolName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(idleTimeOut);
        dataSource.setConnectionTimeout(cnnTimeOut);
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    public static HikariDataSource hikariDataSource(long cnnTimeOut, long idleTimeOut, String poolName) {
        HikariDataSource dataSource = dataSource(cnnTimeOut, idleTimeOut, poolName);
        appendConfig(dataSource);
        return dataSource;
    }
}
