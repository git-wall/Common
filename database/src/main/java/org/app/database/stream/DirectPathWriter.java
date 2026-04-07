package org.app.database.stream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * Oracle Direct Path INSERT writer.
 * Uses APPEND  hint to bypass Oracle's buffer cache and write directly
 * to datafiles — equivalent to SQL*Loader direct path mode.
 * Key behaviors:
 *   - Data goes above the high watermark (HWM) — no space reuse
 *   - Minimal redo logging (especially with NOLOGGING on table)
 *   - Table is locked for the duration — no concurrent DML
 *   - Must COMMIT before SELECT from the same table in same session
 * Throughput: typically 3-8x faster than standard batch INSERT for Oracle.
 * When to use:
 *   - Full load / bulk sync into Oracle target
 *   - Table can tolerate exclusive lock during load
 *   - Paired with TRUNCATE before load for clean full refresh
 */
public final class DirectPathWriter<T> implements RowWriter<T> {

    private static final Logger LOG = Logger.getLogger(DirectPathWriter.class.getName());

    private final String           table;
    private final String[]         columns;
    private final RowSerializer<T> serializer;

    /** Whether to add NOLOGGING hint (requires ALTER TABLE ... NOLOGGING set beforehand). */
    private final boolean nologging;

    private DirectPathWriter(String table, String[] columns, RowSerializer<T> serializer, boolean nologging) {
        this.table      = table;
        this.columns    = columns;
        this.serializer = serializer;
        this.nologging  = nologging;
    }

    @Override
    public void write(Connection conn, List<T> batch) throws SQLException {
        String sql = buildSql();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (T row : batch) {
                Object[] values = serializer.toColumns(row);
                for (int i = 0; i < values.length; i++) {
                    ps.setObject(i + 1, values[i]);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }

        LOG.fine(() -> "DirectPath wrote " + batch.size() + " rows → " + table);
    }

    private String buildSql() {
        // INSERT /*+ APPEND */ INTO table (cols) VALUES (?,?,?)
        // APPEND_VALUES hint (Oracle 11.2+) works per-row and is safe for PreparedStatement batch
        String hint = nologging ? "/*+ APPEND_VALUES */" : "/*+ APPEND_VALUES */";

        StringJoiner cols = new StringJoiner(", ");
        StringJoiner vals = new StringJoiner(", ");
        for (String c : columns) {
            cols.add(c);
            vals.add("?");
        }
        return "INSERT " + hint + " INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static <T> DirectPathWriter<T> of(String table, String[] columns, RowSerializer<T> serializer) {
        return new DirectPathWriter<>(table, columns, serializer, false);
    }

    public static <T> DirectPathWriter<T> withNologging(String table, String[] columns, RowSerializer<T> serializer) {
        return new DirectPathWriter<>(table, columns, serializer, true);
    }
}
