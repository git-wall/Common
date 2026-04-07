package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Synchronous CQL operations — select, insert, update, delete, upsert, LWT.
 * <p>
 * All write operations use <b>prepared statements</b> via {@link CqlPrepared}
 * for performance and safety. Never builds CQL by string concatenation.
 *
 * <pre>{@code
 * CqlOps ops = CqlOps.of(factory);
 *
 * // ── Select ────────────────────────────────────────────────────────────────
 * Optional<User> u = ops.selectOne(
 *     "SELECT * FROM users WHERE id = ?",
 *     row ->; new User(row.getString("id"), row.getString("name")),
 *     "user-123");
 *
 * List<User> all = ops.selectList(
 *     "SELECT * FROM users WHERE status = ?",
 *     row ->; new User(row.getString("id"), row.getString("name")),
 *     "ACTIVE");
 *
 * // ── Insert / Upsert ───────────────────────────────────────────────────────
 * ops.execute("INSERT INTO users (id, name, email) VALUES (?, ?, ?)",
 *     "u1", "Alice", "alice@x.com");
 *
 * // ── Insert IF NOT EXISTS (LWT) ────────────────────────────────────────────
 * boolean inserted = ops.insertIfNotExists(
 *     "INSERT INTO users (id, name) VALUES (?, ?) IF NOT EXISTS",
 *     "u1", "Alice");
 *
 * // ── Update ────────────────────────────────────────────────────────────────
 * ops.execute("UPDATE users SET name = ? WHERE id = ?", "Bob", "u1");
 *
 * // ── Delete ────────────────────────────────────────────────────────────────
 * ops.execute("DELETE FROM users WHERE id = ?", "u1");
 *
 * // ── Count ─────────────────────────────────────────────────────────────────
 * long n = ops.count("SELECT COUNT(*) FROM users WHERE status = ?", "ACTIVE");
 *
 * // ── Raw execute ───────────────────────────────────────────────────────────
 * ResultSet rs = ops.execute("TRUNCATE users");
 * }</pre>
 */
public final class CqlOps {

    private final CqlSession    session;
    private final CqlPrepared prepared;

    private CqlOps(CqlSession session) {
        this.session  = session;
        this.prepared = CqlPrepared.of(session);
    }

    public static CqlOps of(CqlSessionFactory factory) {
        return new CqlOps(factory.session());
    }

    public static CqlOps of(CqlSession session) {
        return new CqlOps(session);
    }

    public CqlSession   session()  { return session; }
    public CqlPrepared  prepared() { return prepared; }

    // -------------------------------------------------------------------------
    // Select
    // -------------------------------------------------------------------------

    /**
     * Select a single row. Returns empty if not found.
     *
     * @param cql    prepared CQL with ? placeholders
     * @param mapper Row → T function
     * @param values positional bind values
     */
    public <T> Optional<T> selectOne(String cql, Function<Row, T> mapper, Object... values) {
        Row row = session.execute(prepared.bind(cql, values)).one();
        return Optional.ofNullable(row).map(mapper);
    }

    /**
     * Select a list of rows.
     */
    public <T> List<T> selectList(String cql, Function<Row, T> mapper, Object... values) {
        ResultSet rs  = session.execute(prepared.bind(cql, values));
        List<T>  list = new ArrayList<>();
        for (Row row : rs) list.add(mapper.apply(row));
        return list;
    }

    /**
     * Select using annotation-based mapping. Requires fields annotated with {@code @CqlMapper.CqlColumn}.
     */
    public <T> Optional<T> selectOne(String cql, Class<T> type, Object... values) {
        Row row = session.execute(prepared.bind(cql, values)).one();
        return Optional.ofNullable(row).map(r -> CqlMapper.map(r, type));
    }

    public <T> List<T> selectList(String cql, Class<T> type, Object... values) {
        ResultSet rs = session.execute(prepared.bind(cql, values));
        return CqlMapper.mapAll(rs, type);
    }

    /**
     * Select a single raw Row (no mapping). Useful for ad-hoc queries.
     */
    public Optional<Row> selectRow(String cql, Object... values) {
        return Optional.ofNullable(session.execute(prepared.bind(cql, values)).one());
    }

    /**
     * Select all rows as raw Row list.
     */
    public List<Row> selectRows(String cql, Object... values) {
        ResultSet rs = session.execute(prepared.bind(cql, values));
        List<Row> rows = new ArrayList<>();
        rs.forEach(rows::add);
        return rows;
    }

    // -------------------------------------------------------------------------
    // Count
    // -------------------------------------------------------------------------

    /**
     * Execute a COUNT query and return the result.
     *
     * <pre>
     * long n = ops.count("SELECT COUNT(*) FROM users WHERE status = ?", "ACTIVE");
     * </pre>
     */
    public long count(String cql, Object... values) {
        Row row = session.execute(prepared.bind(cql, values)).one();
        if (row == null) return 0L;
        return row.getLong(0);
    }

    // -------------------------------------------------------------------------
    // Execute (insert / update / delete / DDL)
    // -------------------------------------------------------------------------

    /**
     * Execute any CQL statement. Returns the full {@link ResultSet}
     * (useful for LWT applied column check).
     */
    public ResultSet execute(String cql, Object... values) {
        if (values.length == 0) {
            return session.execute(SimpleStatement.newInstance(cql));
        }
        return session.execute(prepared.bind(cql, values));
    }

    /**
     * Execute and discard the result. Convenience for fire-and-forget writes.
     */
    public void write(String cql, Object... values) {
        execute(cql, values);
    }

    // -------------------------------------------------------------------------
    // Lightweight Transactions (LWT)
    // -------------------------------------------------------------------------

    /**
     * INSERT IF NOT EXISTS — returns true if inserted, false if row already existed.
     *
     * <pre>
     * boolean created = ops.insertIfNotExists(
     *     "INSERT INTO users (id, email) VALUES (?, ?) IF NOT EXISTS",
     *     uuid, email);
     * </pre>
     */
    public boolean insertIfNotExists(String cql, Object... values) {
        return checkApplied(execute(cql, values));
    }

    /**
     * UPDATE IF condition — returns true if updated, false if condition not met.
     *
     * <pre>
     * boolean updated = ops.updateIf(
     *     "UPDATE users SET name = ? WHERE id = ? IF version = ?",
     *     newName, id, currentVersion);
     * </pre>
     */
    public boolean updateIf(String cql, Object... values) {
        return checkApplied(execute(cql, values));
    }

    /**
     * DELETE IF condition — returns true if deleted.
     */
    public boolean deleteIf(String cql, Object... values) {
        return checkApplied(execute(cql, values));
    }

    /**
     * Generic LWT: executes CQL and returns the {@code [applied]} boolean column.
     * Also returns the existing row values when not applied (for conflict resolution).
     */
    public LwtResult executeIf(String cql, Object... values) {
        Row row = execute(cql, values).one();
        if (row == null) return new LwtResult(false, null);
        boolean applied = row.getBoolean("[applied]");
        return new LwtResult(applied, applied ? null : row);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean checkApplied(ResultSet rs) {
        Row row = rs.one();
        if (row == null) return false;
        try { return row.getBoolean("[applied]"); }
        catch (Exception e) { return true; } // non-LWT statements have no [applied]
    }

    /**
     * Execute a pre-built {@link BoundStatement} directly.
     */
    public ResultSet execute(BoundStatement bs) {
        return session.execute(bs);
    }

    /**
     * Execute a pre-built {@link Statement} directly.
     */
    public ResultSet execute(Statement<?> stmt) {
        return session.execute(stmt);
    }

    // -------------------------------------------------------------------------
    // Result types
    // -------------------------------------------------------------------------

    /**
     * Result of a Lightweight Transaction (LWT) operation.
     * When {@code applied} is false, {@code existingRow} contains the current values.
     */
    public static final class LwtResult {

        private final boolean applied;
        private final Row     existingRow;

        public LwtResult(boolean applied, Row existingRow) {
            this.applied     = applied;
            this.existingRow = existingRow;
        }

        public boolean applied()      { return applied; }
        public Row     existingRow()  { return existingRow; }

        /** Get a field from the existing row (when not applied). */
        public <T> T existing(String column, Class<T> type) {
            if (existingRow == null) return null;
            return type.cast(existingRow.getObject(column));
        }
    }
}
