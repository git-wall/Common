package org.app.database.stream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Auto-detects the fastest WriteStrategy for a given JDBC connection.
 *
 * Detection order (fastest first):
 *   Postgres  → COPY
 *   Oracle    → DIRECT
 *   MySQL     → BATCH  (no native bulk protocol via standard JDBC)
 *   others    → BATCH
 *
 * Can be bypassed by explicitly setting strategy in PipeConfig.
 */
public final class StrategySelector {

    private static final Logger LOG = Logger.getLogger(StrategySelector.class.getName());

    private StrategySelector() {}

    /**
     * Resolve AUTO to a concrete strategy, or return the explicit strategy as-is.
     */
    public static WriteStrategy resolve(WriteStrategy requested, Connection targetConn) {
        if (requested != WriteStrategy.AUTO) return requested;
        return detect(targetConn);
    }

    public static WriteStrategy detect(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String product = meta.getDatabaseProductName().toLowerCase();
            String url     = meta.getURL().toLowerCase();

            if (isPostgres(product, url)) {
                LOG.info("[StrategySelector] Detected PostgreSQL → using COPY protocol");
                return WriteStrategy.COPY;
            }
            if (isOracle(product, url)) {
                LOG.info("[StrategySelector] Detected Oracle → using DIRECT path INSERT");
                return WriteStrategy.DIRECT;
            }
            if (isMySQL(product, url)) {
                LOG.info("[StrategySelector] Detected MySQL → using BATCH INSERT");
                return WriteStrategy.BATCH;
            }

            LOG.info("[StrategySelector] Unknown DB '" + product + "' → fallback to BATCH");
            return WriteStrategy.BATCH;

        } catch (SQLException e) {
            LOG.warning("[StrategySelector] Could not read DB metadata, fallback to BATCH: " + e.getMessage());
            return WriteStrategy.BATCH;
        }
    }

    // ── DB detection helpers ──────────────────────────────────────────────────

    public static boolean isPostgres(String product, String url) {
        return product.contains("postgresql") || url.contains("jdbc:postgresql");
    }

    public static boolean isOracle(String product, String url) {
        return product.contains("oracle") || url.contains("jdbc:oracle");
    }

    public static boolean isMySQL(String product, String url) {
        return product.contains("mysql") || product.contains("mariadb")
            || url.contains("jdbc:mysql") || url.contains("jdbc:mariadb");
    }
}
