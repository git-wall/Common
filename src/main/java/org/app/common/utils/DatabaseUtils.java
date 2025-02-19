package org.app.common.utils;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class DatabaseUtils {
    public static DataSource defaultDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(300000L);
        dataSource.setConnectionTimeout(20000L);
        return dataSource;
    }

    @SneakyThrows
    public static <T> T transaction(Connection connection, Callable<T> callable) {
        try {
            T t = callable.call();
            connection.commit();
            return t;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e);
        } finally {
            connection.close();
        }
    }

    @SneakyThrows
    public static void transaction(Connection connection, Runnable runnable) {
        try {
            runnable.run();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.close();
        }
    }
}
