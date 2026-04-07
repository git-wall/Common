package org.app.database.stream;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * PostgreSQL COPY FROM STDIN writer.
 *
 * Uses org.postgresql.copy.CopyManager via reflection so the postgres driver
 * is an optional compile-time dependency (bring your own at runtime).
 *
 * Format: text mode, tab-delimited, \N for NULL — same as pg_dump default.
 *
 * Throughput: typically 200K–500K rows/sec vs ~60K for JDBC batch.
 *
 * Limitations:
 *   - INSERT only (no UPSERT)
 *   - Requires Postgres JDBC driver at runtime
 *   - Table must exist with matching column layout
 */
public final class CopyWriter<T> implements RowWriter<T> {

    private static final Logger LOG = Logger.getLogger(CopyWriter.class.getName());

    private static final String NULL_MARKER = "\\N";
    private static final char   DELIMITER   = '\t';
    private static final String LINE_END    = "\n";

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final String          table;
    private final String[]        columns;
    private final RowSerializer<T> serializer;

    public CopyWriter(String table, String[] columns, RowSerializer<T> serializer) {
        this.table      = table;
        this.columns    = columns;
        this.serializer = serializer;
    }

    @Override
    public void write(Connection conn, List<T> batch) throws SQLException {
        // Build COPY SQL
        String copySql = buildCopySql();

        // Build text payload
        byte[] payload = buildPayload(batch);

        // Execute via CopyManager (reflection — no hard compile dep on pg driver)
        executeCopy(conn, copySql, payload);

        LOG.fine(() -> "COPY wrote " + batch.size() + " rows → " + table);
    }

    // ── SQL ──────────────────────────────────────────────────────────────────

    private String buildCopySql() {
        StringBuilder sb = new StringBuilder("COPY ").append(table).append(" (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns[i]);
        }
        sb.append(") FROM STDIN WITH (FORMAT text, DELIMITER '\\t', NULL '\\N')");
        return sb.toString();
    }

    // ── Payload builder ───────────────────────────────────────────────────────

    private byte[] buildPayload(List<T> batch) throws SQLException {
        // Estimate ~64 bytes per row for initial capacity
        ByteArrayOutputStream baos = new ByteArrayOutputStream(batch.size() * 64);
        Writer w = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try {
            for (T row : batch) {
                Object[] cols = serializer.toColumns(row);
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0) w.write(DELIMITER);
                    w.write(formatValue(cols[i]));
                }
                w.write(LINE_END);
            }
            w.flush();
        } catch (IOException e) {
            throw new SQLException("Failed to build COPY payload", e);
        }

        return baos.toByteArray();
    }

    private String formatValue(Object v) {
        if (v == null)                        return NULL_MARKER;
        if (v instanceof LocalDate)           return DATE_FMT.format((LocalDate) v);
        if (v instanceof LocalDateTime)       return DATETIME_FMT.format((LocalDateTime) v);
        if (v instanceof Instant)             return DATETIME_FMT.format(
                                                    LocalDateTime.ofInstant((Instant) v, ZoneOffset.UTC));
        if (v instanceof Boolean)             return (Boolean) v ? "t" : "f";
        // Escape special chars: backslash, tab, newline
        String s = v.toString();
        if (s.indexOf('\\') >= 0) s = s.replace("\\", "\\\\");
        if (s.indexOf('\t')  >= 0) s = s.replace("\t",  "\\t");
        if (s.indexOf('\n')  >= 0) s = s.replace("\n",  "\\n");
        if (s.indexOf('\r')  >= 0) s = s.replace("\r",  "\\r");
        return s;
    }

    // ── CopyManager via reflection ────────────────────────────────────────────

    private void executeCopy(Connection conn, String sql, byte[] data) throws SQLException {
        try {
            // Unwrap to PGConnection if wrapped (e.g. HikariCP, DBCP)
            Object pgConn = unwrapPostgres(conn);

            // CopyManager manager = pgConn.getCopyAPI();
            Class<?> pgConnClass = pgConn.getClass();
            Method getCopyApi = findMethod(pgConnClass, "getCopyAPI");
            Object copyManager = getCopyApi.invoke(pgConn);

            // manager.copyIn(sql, inputStream)
            Method copyIn = findMethod(copyManager.getClass(), "copyIn", String.class, InputStream.class);
            InputStream is = new ByteArrayInputStream(data);
            copyIn.invoke(copyManager, sql, is);

        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SQLException(
                "COPY failed — ensure postgresql JDBC driver is on classpath. Cause: " + ex.getMessage(), ex);
        }
    }

    private Object unwrapPostgres(Connection conn) throws Exception {
        // Try standard JDBC unwrap first
        try {
            Class<?> pgConnInterface = Class.forName("org.postgresql.PGConnection");
            if (conn.isWrapperFor(pgConnInterface)) {
                return conn.unwrap(pgConnInterface);
            }
        } catch (ClassNotFoundException ignore) {}

        // Fallback: return as-is and hope driver exposes getCopyAPI directly
        return conn;
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... params) throws NoSuchMethodException {
        // Walk class hierarchy
        Class<?> c = clazz;
        while (c != null) {
            try { return c.getMethod(name, params); } catch (NoSuchMethodException ignore) {}
            for (Class<?> iface : c.getInterfaces()) {
                try { return iface.getMethod(name, params); } catch (NoSuchMethodException ignore) {}
            }
            c = c.getSuperclass();
        }
        throw new NoSuchMethodException(clazz.getName() + "." + name);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static <T> CopyWriter<T> of(String table, String[] columns, RowSerializer<T> serializer) {
        return new CopyWriter<>(table, columns, serializer);
    }
}
