package org.app.database.stream;

/**
 * Write strategy for target DB.
 *
 * AUTO     → StrategySelector detects the best path from JDBC connection metadata
 * COPY     → PostgreSQL COPY protocol (fastest for Postgres full load, INSERT only)
 * DIRECT   → Oracle Direct Path INSERT APPEND  (bypass undo/redo, fastest for Oracle)
 * BATCH    → JDBC addBatch() executeBatch() — universal fallback, works on any JDBC DB
 *
 * Priority (AUTO):  COPY > DIRECT > BATCH
 * Use BATCH explicitly if you need UPSERT/MERGE, triggers, constraints on-the-fly.
 */
public enum WriteStrategy {

    /** Let StrategySelector pick the fastest available path. */
    AUTO,

    /**
     * PostgreSQL COPY FROM STDIN (binary or text).
     * ~4-10x faster than JDBC batch for pure INSERT.
     * Requires: target is Postgres, no UPSERT needed.
     */
    COPY,

    /**
     * Oracle Direct Path via APPEND  hint.
     * Bypasses buffer cache, writes directly to datafiles.
     * Requires: target is Oracle, table must not be in use by other writers.
     */
    DIRECT,

    /**
     * Standard JDBC addBatch() / executeBatch().
     * Universal — works on Postgres, Oracle, MySQL, SQL Server, H2, etc.
     * Slower than COPY/DIRECT but supports any write pattern.
     */
    BATCH
}
