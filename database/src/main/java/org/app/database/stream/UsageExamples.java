package org.app.database.stream;

import io.streampipe.core.*;
import io.streampipe.jdbc.JdbcConnections;
import io.streampipe.strategy.WriteStrategy;
import io.streampipe.writer.BulkWriter;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/**
 * ============================================================
 *  StreamPipe — Complete Usage Reference
 * ============================================================
 *
 * SCENARIO MATRIX:
 *
 *  ┌─────────────────────────────────┬────────────────────────────────────┐
 *  │ Goal                            │ Entry point                        │
 *  ├─────────────────────────────────┼────────────────────────────────────┤
 *  │ Full load A→B, max speed        │ StreamPipe.fastLoad()               │
 *  │ Full load, force strategy       │ fastLoad().strategy(COPY/DIRECT)   │
 *  │ Oracle → Postgres type coerce   │ fastLoad() + TypeConverter         │
 *  │ Filter rows before write        │ builder().transform(row -> ...)    │
 *  │ UPSERT / MERGE                  │ builder() + BulkWriter.upsert()    │
 *  │ Custom write logic              │ builder() + lambda RowWriter       │
 *  └─────────────────────────────────┴────────────────────────────────────┘
 */
public class UsageExamples {

    record Order(long id, String ref, BigDecimal amount, LocalDate orderDate, String status) {}

    // =========================================================================
    // SCENARIO 1 — Full load, AUTO strategy (recommended default)
    //
    //  "Move ALL data from A to B as fast as possible. Auto-pick strategy."
    //
    //  Postgres  → COPY protocol      (~400K rows/sec)
    //  Oracle    → DIRECT path INSERT (~150K rows/sec)
    //  MySQL     → LOAD DATA LOCAL    (~250K rows/sec)
    //  others    → BATCH INSERT       (~60K rows/sec)
    // =========================================================================

    static void scenario1_fastLoad_auto() throws SQLException {
        Connection src = JdbcConnections.open(
            JdbcConnections.postgresUrl("db-a", 5432, "prod"), "ro_user", "secret");
        Connection dst = JdbcConnections.open(
            JdbcConnections.postgresUrl("localhost", 5432, "local"), "app", "pass");

        PipeResult r = StreamPipe
            .fastLoad(dst, "orders",
                new String[]{"id", "ref", "amount", "order_date", "status"},
                row -> new Object[]{row.id(), row.ref(), row.amount(), row.orderDate(), row.status()})

            .source(src, "SELECT id, ref, amount, order_date, status FROM orders")
            .mapper(rs -> new Order(
                rs.getLong("id"),
                rs.getString("ref"),
                rs.getBigDecimal("amount"),
                rs.getDate("order_date").toLocalDate(),
                rs.getString("status")))
            .run();

        System.out.println(r);
        src.close(); dst.close();
    }

    // =========================================================================
    // SCENARIO 2 — Full load, FORCE specific strategy
    //
    //  "I know my target is Postgres. Force COPY, don't auto-detect."
    // =========================================================================

    static void scenario2_fastLoad_forceStrategy() throws SQLException {
        Connection src = JdbcConnections.open("jdbc:oracle:thin:@//oracle:1521/PROD", "ro", "pass");
        Connection dst = JdbcConnections.open("jdbc:postgresql://localhost/warehouse", "etl", "pass");

        PipeResult r = StreamPipe
            .fastLoad(dst, "orders",
                new String[]{"id", "ref", "amount"},
                row -> new Object[]{row.id(), row.ref(), row.amount()})

            .source(src, "SELECT id, ref, amount FROM orders WHERE region = ?", "ASIA")
            .mapper(rs -> new Order(rs.getLong("id"), rs.getString("ref"),
                rs.getBigDecimal("amount"), null, null))

            .strategy(WriteStrategy.COPY)       // ← explicit override
            .config(PipeConfig.builder()
                .fetchSize(10_000)
                .batchSize(5_000)
                .commitPerBatch(false)          // single commit at end
                .build())
            .run();

        System.out.println(r);
        src.close(); dst.close();
    }

    // =========================================================================
    // SCENARIO 3 — Oracle → Postgres with TypeConverter
    //
    //  Oracle CLOB → String, Oracle DATE → LocalDate
    // =========================================================================

    record LegacyRow(long id, String notes, LocalDate createdAt) {}

    static void scenario3_oracleToPostgres_types() throws SQLException {
        TypeConverter tc = TypeConverter.defaults();

        Connection oracle = JdbcConnections.open(
            JdbcConnections.oracleUrl("oracle-host", 1521, "ORCLPDB"), "ro", "pass");
        Connection pg = JdbcConnections.open(
            JdbcConnections.postgresUrl("localhost", 5432, "dw"), "etl", "pass");

        JdbcConnections.configureOracleStreamingSource(oracle);

        PipeResult r = StreamPipe
            .fastLoad(pg, "legacy_data",
                new String[]{"id", "notes", "created_at"},
                row -> new Object[]{row.id(), row.notes(), row.createdAt()})

            .source(oracle, "SELECT id, notes, created_at FROM legacy_tbl")
            .mapper(rs -> new LegacyRow(
                rs.getLong("id"),
                tc.convert(rs.getObject("notes"),     String.class),    // CLOB → String
                tc.convert(rs.getObject("created_at"), LocalDate.class) // Oracle DATE → LocalDate
            ))
            .run();

        System.out.println(r);
        oracle.close(); pg.close();
    }

    // =========================================================================
    // SCENARIO 4 — Filter + transform (builder, full control)
    //
    //  "Only sync high-value orders, enrich status label."
    //  Return null from transform → row is skipped (counted in rowsSkipped)
    // =========================================================================

    record LocalOrder(long id, String ref, BigDecimal amount, String enrichedStatus) {}

    static void scenario4_filterAndTransform() throws SQLException {
        Connection src = JdbcConnections.open("jdbc:postgresql://db-a/prod",    "ro", "pass");
        Connection dst = JdbcConnections.open("jdbc:postgresql://localhost/app", "rw", "pass");

        PipeResult r = StreamPipe.<Order, LocalOrder>builder()
            .source(src, "SELECT id, ref, amount, order_date, status FROM orders")
            .mapper(rs -> new Order(
                rs.getLong("id"), rs.getString("ref"), rs.getBigDecimal("amount"),
                rs.getDate("order_date").toLocalDate(), rs.getString("status")))

            .transform(o -> {
                if (o.amount().compareTo(BigDecimal.valueOf(500)) < 0) return null; // skip
                String enriched = "PENDING".equals(o.status()) ? "AWAITING_APPROVAL" : o.status();
                return new LocalOrder(o.id(), o.ref(), o.amount(), enriched);
            })

            .target(dst, BulkWriter.postgresUpsert(
                "local_orders",
                new String[]{"id", "ref", "amount", "status"},
                new String[]{"id"},
                row -> new Object[]{row.id(), row.ref(), row.amount(), row.enrichedStatus()}))

            .config(PipeConfig.builder().fetchSize(2000).batchSize(500).build())
            .run();

        System.out.printf("read=%d written=%d filtered=%d%n",
            r.getRowsRead(), r.getRowsWritten(), r.getRowsSkipped());
        src.close(); dst.close();
    }

    // =========================================================================
    // SCENARIO 5 — Oracle target, Direct Path INSERT (auto-detected)
    // =========================================================================

    static void scenario5_toOracle_directPath() throws SQLException {
        Connection src = JdbcConnections.open("jdbc:postgresql://db-a/prod", "ro", "pass");
        Connection oracle = JdbcConnections.open(
            JdbcConnections.oracleUrl("oracle-b", 1521, "ORCLPDB"), "etl", "pass");

        // Auto-detects Oracle → DIRECT path INSERT /*+ APPEND_VALUES */
        PipeResult r = StreamPipe
            .fastLoad(oracle, "ORDERS",
                new String[]{"ORDER_ID", "REF", "AMOUNT", "ORDER_DATE"},
                row -> new Object[]{row.id(), row.ref(), row.amount(), row.orderDate()})

            .source(src, "SELECT id, ref, amount, order_date FROM orders")
            .mapper(rs -> new Order(rs.getLong("id"), rs.getString("ref"),
                rs.getBigDecimal("amount"), rs.getDate("order_date").toLocalDate(), null))

            .config(PipeConfig.builder()
                .fetchSize(5_000).batchSize(1_000)
                .commitPerBatch(true) // Oracle: commit required before another session reads
                .build())
            .run();

        System.out.println(r);
        src.close(); oracle.close();
    }

    // =========================================================================
    // SCENARIO 6 — Custom RowWriter (no target DB: call API, write file...)
    // =========================================================================

    static void scenario6_customWriter() throws SQLException {
        Connection src = JdbcConnections.open("jdbc:postgresql://db-a/prod", "ro", "pass");

        PipeResult r = StreamPipe.<Order, Order>builder()
            .source(src, "SELECT id, ref, amount, order_date, status FROM orders")
            .mapper(rs -> new Order(rs.getLong("id"), rs.getString("ref"),
                rs.getBigDecimal("amount"), rs.getDate("order_date").toLocalDate(), rs.getString("status")))

            .target(src, (conn, batch) -> { // conn unused
                System.out.printf("Batch of %d rows%n", batch.size());
                // externalApiClient.sendBatch(batch);
                // csvWriter.writeRows(batch);
            })

            .config(PipeConfig.builder().batchSize(200).commitPerBatch(false).build())
            .run();

        System.out.println(r);
        src.close();
    }

    public static void main(String[] args) {
        System.out.println("Adjust connection strings and run individual scenarios.");
    }
}
