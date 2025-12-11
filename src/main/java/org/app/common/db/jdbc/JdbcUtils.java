package org.app.common.db.jdbc;

import lombok.SneakyThrows;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Bộ tiện ích JDBC với xử lý lỗi toàn diện
 * Bao gồm: timeout, retry, fallback, batch conflict handling
 */
public class JdbcUtils {

    public static PreparedStatement statementTimeout(Connection conn, String sql, int timeoutSeconds) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setQueryTimeout(timeoutSeconds);
        return ps;
    }

    // ==================== RETRY MECHANISM ====================

    /**
     * Retry configuration
     */
    public static class RetryConfig {
        public int maxAttempts = 3;
        public long initialDelayMs = 1000;
        public double backoffMultiplier = 2.0;
        public long maxDelayMs = 30000;
        public Set<String> retryableSQLStates = new HashSet<>(Arrays.asList(
            "08S01", // Communication link failure
            "08003", // Connection does not exist
            "08006", // Connection failure
            "40001", // Serialization failure
            "40P01", // Deadlock detected
            "57P03", // Cannot connect now
            "HYT00", // Timeout expired
            "HYT01", // Connection timeout
            "08001", // SQL-client unable to establish SQL-connection
            "08004", // SQL-server rejected establishment of SQL-connection
            "08007", // Transaction resolution unknown
            "1205",  // MySQL deadlock
            "1213"   // MySQL lock wait timeout exceeded
        ));
    }

    @SneakyThrows
    public static <T> T executeWithRetry(RetryableOperation<T> operation, RetryConfig config) {
        return executeWithRetry(operation, config, 0);
    }

    /**
     * Thực thi với retry khi gặp lỗi có thể retry
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, RetryConfig config, int currentAttempt) throws Exception {
        try {
            return operation.execute();
        } catch (SQLException e) {
            if (shouldRetry(e, config) && currentAttempt < config.maxAttempts) {
                return handleRetry(operation, config, currentAttempt);
            }
            throw e;
        } catch (Exception e) {
            if (shouldRetry(e) && currentAttempt < config.maxAttempts) {
                return handleRetry(operation, config, currentAttempt);
            }
            throw e;
        }
    }

    private static <T> T handleRetry(RetryableOperation<T> operation,
                                     RetryConfig config,
                                     int currentAttempt) throws Exception {
        // Tính delay và chờ
        long delay = calculateDelay(currentAttempt, config);
        System.out.println("Waiting " + delay + "ms before retry...");
        Thread.sleep(delay);

        // Gọi đệ quy để retry
        return executeWithRetry(operation, config, currentAttempt + 1);
    }

    private static long calculateDelay(int attempt, RetryConfig config) {
        long delay = (long) (config.initialDelayMs * Math.pow(config.backoffMultiplier, attempt - 1));
        return Math.min(delay, config.maxDelayMs);
    }

    private static boolean shouldRetry(SQLException e, RetryConfig config) {
        String sqlState = e.getSQLState();
        return sqlState != null && config.retryableSQLStates.contains(sqlState);
    }

    private static boolean shouldRetry(Exception e) {
        return e instanceof SQLTimeoutException || e instanceof SocketTimeoutException || e instanceof ConnectException;
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}
