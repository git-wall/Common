package org.app.database.mybatis;

import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Thin, zero-reflection execution engine over MyBatis Dynamic SQL.
 *
 * <h3>Insert API — important distinction</h3>
 *
 * MyBatis Dynamic SQL has two insert APIs:
 *
 * <pre>
 * // ── (A) GeneralInsertStatementProvider — single row, JDBC-compatible ─────
 * //   Built by: insertInto(table).set(col).toValue(val)...
 * //   Renders:  INSERT INTO t (name, email) VALUES (?, ?)   ← uses ?
 * //   Has:      getInsertStatement() + getParameters()      ← JDBC-bindable
 * //   Use for:  inserting one row, getting auto-generated key
 *
 * GeneralInsertStatementProvider stmt = insertInto(UserTable.TABLE)
 *     .set(UserTable.TABLE.name).toValue("Alice")
 *     .set(UserTable.TABLE.email).toValue("alice@x.com")
 *     .build().render(RenderingStrategies.MYBATIS3);
 *
 * SqlResult r = db.insert(stmt);
 * Long id = r.generatedKey(Long.class);
 *
 *
 * // ── (B) insertMultiValues — N rows, single round-trip, fastest ───────────
 * //   Built by: caller supplies table name + column list + List<Object[]>
 * //   DynSql builds: INSERT INTO t (a,b) VALUES (?,?),(?,?),(?,?)
 * //   Zero reflection. Plain JDBC. One round-trip.
 *
 * List&lt;Object[]&gt; rows = users.stream()
 *     .map(u -&gt; new Object[]{ u.name, u.email })
 *     .collect(Collectors.toList());
 *
 * db.insertMultiValues("users",
 *     Arrays.asList("name", "email"),
 *     rows);
 *
 *
 * // ── (C) rawBatch — addBatch/executeBatch, also zero reflection ───────────
 * //   Same SQL per call, different params. Multiple round-trips but still fast.
 * //   Use when row count is very large (multi-values SQL can exceed DB limits).
 *
 * db.rawBatch(
 *     "INSERT INTO users (name, email) VALUES (?, ?)",
 *     rows);
 *
 *
 * // ── MultiRowInsertStatementProvider — NOT supported here ─────────────────
 * //   insertMultiple(...).into(t).map(col).toProperty("field")
 * //   Renders:  INSERT INTO t (name) VALUES (#{records[0].name})  ← ORM syntax
 * //   Has:      getInsertStatement() + getRows()  but NO getParameters()
 * //   ✗ Not bindable with plain JDBC PreparedStatement.
 * //   ✗ Only works with MyBatis ORM session or Spring NamedParameterJdbcTemplate.
 * </pre>
 *
 * <h3>Architecture</h3>
 * <pre>
 *  Caller defines table/column metadata (SqlTable / SqlColumn)
 *      │
 *      ▼
 *  MyBatis Dynamic SQL builds SQL + parameter map
 *      │
 *      ▼
 *  DynSql executes via JDBC PreparedStatement
 *      │  • PreparedStatement cache (per connection, LRU)
 *      │  • Direct type binding — no reflection
 *      │  • Caller-supplied RowMapper — no reflection
 *      ▼
 *  Connection pool (HikariCP / any DataSource)
 * </pre>
 *
 * <h3>RenderingStrategy</h3>
 * Always use {@code RenderingStrategies.MYBATIS3}.
 * It renders {@code #{paramN}} placeholders for non-general inserts and
 * {@code ?} for general inserts and multi-row inserts — both work correctly
 * with our parameter binding logic.
 */
public final class DynSql {

    private final DataSource dataSource;
    private final StmtCache cache;

    private DynSql(DataSource dataSource, int cacheSize) {
        this.dataSource = dataSource;
        this.cache      = new StmtCache(cacheSize);
    }

    /** Create with default LRU statement cache size (512). */
    public static DynSql of(DataSource dataSource) {
        return new DynSql(dataSource, 512);
    }

    /** Create with explicit statement cache size. */
    public static DynSql of(DataSource dataSource, int cacheSize) {
        return new DynSql(dataSource, cacheSize);
    }

    // =========================================================================
    // Transaction
    // =========================================================================

    /**
     * Open a manual transaction scope. Use with try-with-resources.
     * Auto-rolls back on close if {@link TransactionScope#commit()} was never called.
     *
     * <pre>
     * try (TransactionScope tx = db.transaction()) {
     *     db.insert(tx, insertStmt);
     *     db.update(tx, updateStmt);
     *     tx.commit();
     * }
     * </pre>
     */
    public TransactionScope transaction() {
        try {
            return new TransactionScope(dataSource.getConnection());
        } catch (SQLException e) {
            throw new DynSqlException("Cannot open transaction", e);
        }
    }

    /**
     * Run work inside a transaction. Commits on success, rolls back + rethrows on exception.
     *
     * <pre>
     * db.transact(tx -&gt; {
     *     db.insert(tx, stmt1);
     *     db.insert(tx, stmt2);
     * });
     * </pre>
     */
    public void transact(Consumer<TransactionScope> work) {
        try (TransactionScope tx = transaction()) {
            work.accept(tx);
            tx.commit();
        } catch (DynSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new DynSqlException("Transaction rolled back", e);
        }
    }

    /**
     * Run work inside a transaction and return a value.
     *
     * <pre>
     * Long id = db.transact(tx -&gt; {
     *     SqlResult r = db.insert(tx, insertStmt);
     *     db.insert(tx, insertAuditStmt);
     *     return r.generatedKey(Long.class);
     * });
     * </pre>
     */
    public <T> T transact(Function<TransactionScope, T> work) {
        try (TransactionScope tx = transaction()) {
            T result = work.apply(tx);
            tx.commit();
            return result;
        } catch (DynSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new DynSqlException("Transaction rolled back", e);
        }
    }

    // =========================================================================
    // SELECT — list
    // =========================================================================

    /**
     * Execute SELECT and return all rows mapped by {@code mapper}.
     */
    public <T> List<T> selectList(SelectStatementProvider stmt, RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection()) {
            return doSelectList(conn, stmt.getSelectStatement(), stmt.getParameters(), mapper);
        } catch (SQLException e) {
            throw new DynSqlException("selectList failed", e);
        }
    }

    public <T> List<T> selectList(TransactionScope tx,
                                  SelectStatementProvider stmt,
                                  RowMapper<T> mapper) {
        return doSelectList(tx.connection(),
            stmt.getSelectStatement(), stmt.getParameters(), mapper);
    }

    // =========================================================================
    // SELECT — one row
    // =========================================================================

    /**
     * Execute SELECT expecting zero or one row.
     * Throws {@link DynSqlException} if more than one row is returned.
     */
    public <T> Optional<T> selectOne(SelectStatementProvider stmt, RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection()) {
            return doSelectOne(conn, stmt.getSelectStatement(), stmt.getParameters(), mapper);
        } catch (SQLException e) {
            throw new DynSqlException("selectOne failed", e);
        }
    }

    public <T> Optional<T> selectOne(TransactionScope tx,
                                     SelectStatementProvider stmt,
                                     RowMapper<T> mapper) {
        return doSelectOne(tx.connection(),
            stmt.getSelectStatement(), stmt.getParameters(), mapper);
    }

    // =========================================================================
    // SELECT — scalar
    // =========================================================================

    /**
     * Execute COUNT(*) or any scalar SELECT returning a single {@code long}.
     * Returns 0 if no row is found.
     *
     * <pre>
     * long n = db.selectCount(
     *     select(count()).from(UserTable.TABLE)
     *         .where(UserTable.TABLE.status, isEqualTo("ACTIVE"))
     *         .build().render(MYBATIS3));
     * </pre>
     */
    public long selectCount(SelectStatementProvider stmt) {
        try (Connection conn = dataSource.getConnection()) {
            return doSelectScalarLong(conn, stmt.getSelectStatement(), stmt.getParameters());
        } catch (SQLException e) {
            throw new DynSqlException("selectCount failed", e);
        }
    }

    public long selectCount(TransactionScope tx, SelectStatementProvider stmt) {
        return doSelectScalarLong(tx.connection(),
            stmt.getSelectStatement(), stmt.getParameters());
    }

    /**
     * Execute a scalar SELECT and map the first row.
     * Use for SUM, MAX, MIN, or any custom scalar projection.
     *
     * <pre>
     * Optional&lt;BigDecimal&gt; total = db.selectScalar(
     *     select(sum(amount)).from(OrderTable.TABLE).build().render(MYBATIS3),
     *     rs -&gt; rs.getBigDecimal(1));
     * </pre>
     */
    public <T> Optional<T> selectScalar(SelectStatementProvider stmt, RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection()) {
            return doSelectScalar(conn, stmt.getSelectStatement(), stmt.getParameters(), mapper);
        } catch (SQLException e) {
            throw new DynSqlException("selectScalar failed", e);
        }
    }

    public <T> Optional<T> selectScalar(TransactionScope tx,
                                        SelectStatementProvider stmt,
                                        RowMapper<T> mapper) {
        return doSelectScalar(tx.connection(),
            stmt.getSelectStatement(), stmt.getParameters(), mapper);
    }

    // =========================================================================
    // SELECT — paged
    // =========================================================================

    /**
     * Run two queries — COUNT first, then the data page.
     * Caller applies LIMIT/OFFSET to {@code dataStmt} and provides
     * the equivalent COUNT(*) as {@code countStmt}.
     *
     * <pre>
     * int page = 1, pageSize = 20, offset = 0;
     *
     * SelectStatementProvider data = select(id, name)
     *     .from(UserTable.TABLE)
     *     .where(status, isEqualTo("ACTIVE"))
     *     .orderBy(createdAt.descending())
     *     .limit(pageSize).offset(offset)
     *     .build().render(MYBATIS3);
     *
     * SelectStatementProvider count = select(count())
     *     .from(UserTable.TABLE)
     *     .where(status, isEqualTo("ACTIVE"))
     *     .build().render(MYBATIS3);
     *
     * PageResult&lt;User&gt; result = db.selectPage(data, count, USER_MAPPER, page, pageSize);
     * result.rows()       // List for this page
     * result.total()      // total matching rows
     * result.hasNext()    // more pages?
     * </pre>
     */
    public <T> PageResult<T> selectPage(SelectStatementProvider dataStmt,
                                        SelectStatementProvider countStmt,
                                        RowMapper<T> mapper,
                                        int page,
                                        int pageSize) {
        try (Connection conn = dataSource.getConnection()) {
            long total = doSelectScalarLong(conn,
                countStmt.getSelectStatement(), countStmt.getParameters());
            List<T> rows = doSelectList(conn,
                dataStmt.getSelectStatement(), dataStmt.getParameters(), mapper);
            return new PageResult<>(rows, total, page, pageSize);
        } catch (SQLException e) {
            throw new DynSqlException("selectPage failed", e);
        }
    }

    public <T> PageResult<T> selectPage(TransactionScope tx,
                                        SelectStatementProvider dataStmt,
                                        SelectStatementProvider countStmt,
                                        RowMapper<T> mapper,
                                        int page,
                                        int pageSize) {
        long total = doSelectScalarLong(tx.connection(),
            countStmt.getSelectStatement(), countStmt.getParameters());
        List<T> rows = doSelectList(tx.connection(),
            dataStmt.getSelectStatement(), dataStmt.getParameters(), mapper);
        return new PageResult<>(rows, total, page, pageSize);
    }

    // =========================================================================
    // SELECT — streaming
    // =========================================================================

    /**
     * Stream all matching rows to {@code consumer} one-by-one, no List accumulation.
     * Use for large exports, ETL, or million-row processing.
     *
     * <pre>
     * db.selectStream(stmt, USER_MAPPER, user -&gt; csvWriter.write(user));
     * </pre>
     */
    public <T> void selectStream(SelectStatementProvider stmt,
                                 RowMapper<T> mapper,
                                 Consumer<T> consumer) {
        String sql = stmt.getSelectStatement();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE); // MySQL server-side cursor
            bindDynParams(ps, stmt.getParameters());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) consumer.accept(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new DynSqlException("selectStream failed: " + sql, e);
        }
    }

    // =========================================================================
    // EXISTS
    // =========================================================================

    /**
     * Return true if the SELECT returns at least one row.
     *
     * <pre>
     * boolean exists = db.exists(
     *     select(constant("1")).from(UserTable.TABLE)
     *         .where(UserTable.TABLE.id, isEqualTo(42L))
     *         .limit(1).build().render(MYBATIS3));
     * </pre>
     */
    public boolean exists(SelectStatementProvider stmt) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = cache.get(conn, stmt.getSelectStatement());
            ps.setMaxRows(1);
            bindDynParams(ps, stmt.getParameters());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DynSqlException("exists failed", e);
        }
    }

    public boolean exists(TransactionScope tx, SelectStatementProvider stmt) {
        try {
            PreparedStatement ps = cache.get(tx.connection(), stmt.getSelectStatement());
            ps.setMaxRows(1);
            bindDynParams(ps, stmt.getParameters());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DynSqlException("exists (tx) failed", e);
        }
    }

    // =========================================================================
    // INSERT — single row via GeneralInsertStatementProvider
    // =========================================================================

    /**
     * Execute a single-row INSERT built with {@code insertInto(table).set(col).toValue(val)}.
     *
     * <p><b>Why GeneralInsertStatementProvider?</b><br>
     * {@code insertInto(...).set(...).toValue(...)} renders SQL with {@code ?} placeholders
     * and exposes {@code getParameters()} — making it directly JDBC-bindable without reflection.
     * This is the only single-row INSERT path supported here.
     *
     * <pre>
     * // Define your columns (once, as static final fields in a SqlTable subclass):
     * // SqlColumn&lt;Long&gt;   id    = column("id");
     * // SqlColumn&lt;String&gt; name  = column("name");
     * // SqlColumn&lt;String&gt; email = column("email");
     *
     * GeneralInsertStatementProvider stmt = insertInto(UserTable.TABLE)
     *     .set(UserTable.TABLE.name).toValue("Alice")
     *     .set(UserTable.TABLE.email).toValue("alice@x.com")
     *     .set(UserTable.TABLE.status).toValue("ACTIVE")
     *     .build().render(RenderingStrategies.MYBATIS3);
     *
     * // Renders: INSERT INTO users (name, email, status) VALUES (?, ?, ?)
     * // Parameters: {p1="Alice", p2="alice@x.com", p3="ACTIVE"}
     *
     * SqlResult r = db.insert(stmt);
     * Long id = r.generatedKey(Long.class);  // auto-increment PK
     * </pre>
     */
    public SqlResult insert(GeneralInsertStatementProvider stmt) {
        try (Connection conn = dataSource.getConnection()) {
            return doInsert(conn, stmt.getInsertStatement(), stmt.getParameters());
        } catch (SQLException e) {
            throw new DynSqlException("insert failed", e);
        }
    }

    public SqlResult insert(TransactionScope tx, GeneralInsertStatementProvider stmt) {
        return doInsert(tx.connection(), stmt.getInsertStatement(), stmt.getParameters());
    }

    // =========================================================================
    // INSERT — multi-values (VALUES (?,?),(?,?),... — single round-trip, fastest)
    // =========================================================================

    /**
     * Execute a bulk INSERT as a single multi-values statement — one round-trip.
     *
     * <p><b>Why not MultiRowInsertStatementProvider?</b><br>
     * {@code MultiRowInsertStatementProvider} renders {@code #{records[0].name}} style
     * placeholders (MyBatis ORM syntax) and stores values inside {@code getRows()} —
     * it has <b>no {@code getParameters()}</b> and is not bindable with plain JDBC.
     * It only works with MyBatis ORM session or Spring {@code NamedParameterJdbcTemplate}.
     *
     * <p>This method builds the multi-values SQL directly from a caller-supplied column
     * list and row data — zero reflection, plain JDBC, one round-trip:
     * <pre>
     * INSERT INTO users (name, email) VALUES (?,?),(?,?),(?,?)
     * </pre>
     *
     * <h4>Usage</h4>
     * <pre>
     * // columns: exactly the column names to insert, in the same order as each Object[] row
     * List&lt;String&gt; cols = Arrays.asList("name", "email", "status");
     *
     * List&lt;Object[]&gt; rows = users.stream()
     *     .map(u -&gt; new Object[]{ u.name, u.email, "ACTIVE" })
     *     .collect(Collectors.toList());
     *
     * db.insertMultiValues("users", cols, rows);
     *
     * // Renders and executes:
     * // INSERT INTO users (name, email, status) VALUES (?,?,?),(?,?,?),(?,?,?)
     * // with all values bound positionally — one PreparedStatement, one round-trip.
     * </pre>
     *
     * <p><b>Performance comparison</b>
     * <pre>
     * insertMultiValues  → 1 round-trip,  1 parse/plan    ← fastest
     * rawBatch           → N round-trips, 1 parse/plan    ← still fast, more flexible
     * N × insert         → N round-trips, N parse/plans   ← slowest
     * </pre>
     *
     * @param table   table name (not validated — caller's responsibility)
     * @param columns column names in order matching each {@code rows} element
     * @param rows    each element is one row's values, must match {@code columns} length
     */
    public SqlResult insertMultiValues(String table, List<String> columns, List<Object[]> rows) {
        if (rows.isEmpty()) return new SqlResult(new int[0]);
        try (Connection conn = dataSource.getConnection()) {
            return doInsertMultiValues(conn, table, columns, rows);
        } catch (SQLException e) {
            throw new DynSqlException("insertMultiValues failed: " + table, e);
        }
    }

    public SqlResult insertMultiValues(TransactionScope tx,
                                       String table,
                                       List<String> columns,
                                       List<Object[]> rows) {
        if (rows.isEmpty()) return new SqlResult(new int[0]);
        return doInsertMultiValues(tx.connection(), table, columns, rows);
    }

    private SqlResult doInsertMultiValues(Connection conn,
                                          String table,
                                          List<String> columns,
                                          List<Object[]> rows) {
        // Build: INSERT INTO <table> (<col1>,<col2>,...) VALUES (?,?,...),(?,?,...), ...
        String colList = String.join(", ", columns);

        // One (?,?,?) tuple per row
        StringBuilder rowPlaceholder = new StringBuilder("(");
        for (int c = 0; c < columns.size(); c++) {
            if (c > 0) rowPlaceholder.append(", ");
            rowPlaceholder.append("?");
        }
        rowPlaceholder.append(")");
        String tuple = rowPlaceholder.toString();

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" (").append(colList).append(") VALUES ");
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) sql.append(", ");
            sql.append(tuple);
        }

        String sqlStr = sql.toString();
        try {
            // Do NOT cache this statement — SQL length varies with row count
            PreparedStatement ps = conn.prepareStatement(
                sqlStr, java.sql.Statement.RETURN_GENERATED_KEYS);
            int idx = 1;
            for (Object[] row : rows) {
                for (Object val : row) setParam(ps, idx++, val);
            }
            int affected = ps.executeUpdate();
            return new SqlResult(affected, readGeneratedKeys(ps));
        } catch (SQLException e) {
            throw new DynSqlException("insertMultiValues execute failed: " + sqlStr, e);
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    /**
     * Execute an UPDATE statement.
     *
     * <pre>
     * db.update(
     *     update(UserTable.TABLE)
     *         .set(UserTable.TABLE.status).equalToValue("INACTIVE")
     *         .where(UserTable.TABLE.id, isEqualTo(42L))
     *         .build().render(MYBATIS3));
     * </pre>
     */
    public SqlResult update(UpdateStatementProvider stmt) {
        try (Connection conn = dataSource.getConnection()) {
            return doWrite(conn, stmt.getUpdateStatement(), stmt.getParameters());
        } catch (SQLException e) {
            throw new DynSqlException("update failed", e);
        }
    }

    public SqlResult update(TransactionScope tx, UpdateStatementProvider stmt) {
        return doWrite(tx.connection(), stmt.getUpdateStatement(), stmt.getParameters());
    }

    // =========================================================================
    // UPDATE — batch
    // =========================================================================

    /**
     * Execute multiple UPDATEs as a JDBC batch.
     * All statements must share the same SQL template.
     * For heterogeneous updates, call {@link #update} in a {@link #transact} block instead.
     *
     * <pre>
     * List&lt;UpdateStatementProvider&gt; stmts = users.stream()
     *     .map(u -&gt; update(UserTable.TABLE)
     *         .set(UserTable.TABLE.name).equalToValue(u.name)
     *         .where(UserTable.TABLE.id, isEqualTo(u.id))
     *         .build().render(MYBATIS3))
     *     .collect(Collectors.toList());
     *
     * db.updateBatch(stmts);
     * </pre>
     */
    public SqlResult updateBatch(List<UpdateStatementProvider> stmts) {
        if (stmts.isEmpty()) return new SqlResult(new int[0]);
        try (Connection conn = dataSource.getConnection()) {
            return doUpdateBatch(conn, stmts, true);
        } catch (SQLException e) {
            throw new DynSqlException("updateBatch failed", e);
        }
    }

    public SqlResult updateBatch(TransactionScope tx, List<UpdateStatementProvider> stmts) {
        if (stmts.isEmpty()) return new SqlResult(new int[0]);
        return doUpdateBatch(tx.connection(), stmts, false);
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Execute a DELETE statement.
     *
     * <pre>
     * db.delete(
     *     deleteFrom(UserTable.TABLE)
     *         .where(UserTable.TABLE.status, isEqualTo("DELETED"))
     *         .build().render(MYBATIS3));
     * </pre>
     */
    public SqlResult delete(DeleteStatementProvider stmt) {
        try (Connection conn = dataSource.getConnection()) {
            return doWrite(conn, stmt.getDeleteStatement(), stmt.getParameters());
        } catch (SQLException e) {
            throw new DynSqlException("delete failed", e);
        }
    }

    public SqlResult delete(TransactionScope tx, DeleteStatementProvider stmt) {
        return doWrite(tx.connection(), stmt.getDeleteStatement(), stmt.getParameters());
    }

    // =========================================================================
    // Raw SQL — escape hatch
    // =========================================================================

    /**
     * Execute a raw SQL SELECT with positional {@code ?} parameters.
     * Use for CTEs, window functions, recursive queries, or DB-specific syntax.
     *
     * <pre>
     * List&lt;User&gt; result = db.rawSelectList(
     *     "SELECT * FROM users WHERE created_at &gt; ? AND status = ?",
     *     USER_MAPPER, cutoff, "ACTIVE");
     * </pre>
     */
    public <T> List<T> rawSelectList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = cache.get(conn, sql);
            bindPositional(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) result.add(mapper.map(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawSelectList failed: " + sql, e);
        }
    }

    public <T> List<T> rawSelectList(TransactionScope tx, String sql,
                                     RowMapper<T> mapper, Object... params) {
        try {
            PreparedStatement ps = cache.get(tx.connection(), sql);
            bindPositional(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) result.add(mapper.map(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawSelectList (tx) failed: " + sql, e);
        }
    }

    public <T> Optional<T> rawSelectOne(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = cache.get(conn, sql);
            bindPositional(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawSelectOne failed: " + sql, e);
        }
    }

    public <T> Optional<T> rawSelectOne(TransactionScope tx, String sql,
                                        RowMapper<T> mapper, Object... params) {
        try {
            PreparedStatement ps = cache.get(tx.connection(), sql);
            bindPositional(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawSelectOne (tx) failed: " + sql, e);
        }
    }

    /** Raw SELECT returning first column of first row as long. Returns 0 if no row. */
    public long rawSelectLong(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = cache.get(conn, sql);
            bindPositional(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawSelectLong failed: " + sql, e);
        }
    }

    /**
     * Execute a raw INSERT / UPDATE / DELETE with positional {@code ?} parameters.
     *
     * <pre>
     * SqlResult r = db.rawExecute(
     *     "INSERT INTO audit (entity, action) VALUES (?, ?)", "USER", "LOGIN");
     * Long auditId = r.generatedKey(Long.class);
     * </pre>
     */
    public SqlResult rawExecute(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
            return doRawWrite(conn, sql, params);
        } catch (SQLException e) {
            throw new DynSqlException("rawExecute failed: " + sql, e);
        }
    }

    public SqlResult rawExecute(TransactionScope tx, String sql, Object... params) {
        return doRawWrite(tx.connection(), sql, params);
    }

    /**
     * Execute many rows of the same SQL as a JDBC addBatch/executeBatch.
     * Each element of {@code paramSets} is one row's {@code ?} parameters.
     *
     * <p><b>Performance note:</b> For bulk INSERT, prefer
     * {@link #insertMultiValues(String, List, List)} which sends a single
     * {@code INSERT INTO t VALUES (...),(...),...} statement — faster than addBatch
     * because it uses one round-trip and one plan. Use {@code rawBatch} when:
     * <ul>
     *   <li>The statement is not an INSERT (e.g. bulk UPDATE or DELETE)</li>
     *   <li>Row count is very large and multi-values SQL would exceed DB limits</li>
     * </ul>
     *
     * <pre>
     * List&lt;Object[]&gt; rows = users.stream()
     *     .map(u -&gt; new Object[]{ u.name, u.email })
     *     .collect(Collectors.toList());
     *
     * db.rawBatch("INSERT INTO users (name, email) VALUES (?, ?)", rows);
     * </pre>
     */
    public SqlResult rawBatch(String sql, List<Object[]> paramSets) {
        if (paramSets.isEmpty()) return new SqlResult(new int[0]);
        try (Connection conn = dataSource.getConnection()) {
            return doRawBatch(conn, sql, paramSets, true);
        } catch (SQLException e) {
            throw new DynSqlException("rawBatch failed: " + sql, e);
        }
    }

    public SqlResult rawBatch(TransactionScope tx, String sql, List<Object[]> paramSets) {
        if (paramSets.isEmpty()) return new SqlResult(new int[0]);
        return doRawBatch(tx.connection(), sql, paramSets, false);
    }

    // =========================================================================
    // Cache management
    // =========================================================================

    /** Evict all cached statements for a given SQL (e.g. after schema change). */
    public void invalidateCache(String sql) { cache.invalidateSql(sql); }

    /** Clear entire statement cache. */
    public void clearCache() { cache.clear(); }

    /** Current number of cached statements. */
    public int cacheSize()   { return cache.size(); }

    // =========================================================================
    // Private — all JDBC work
    // =========================================================================

    private <T> List<T> doSelectList(Connection conn, String sql,
                                     Map<String, Object> params, RowMapper<T> mapper) {
        try {
            PreparedStatement ps = cache.get(conn, sql);
            bindDynParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) result.add(mapper.map(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new DynSqlException("selectList failed: " + sql, e);
        }
    }

    private <T> Optional<T> doSelectOne(Connection conn, String sql,
                                        Map<String, Object> params, RowMapper<T> mapper) {
        try {
            PreparedStatement ps = cache.get(conn, sql);
            bindDynParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                T val = mapper.map(rs);
                if (rs.next()) throw new DynSqlException(
                    "selectOne returned more than one row: " + sql);
                return Optional.of(val);
            }
        } catch (DynSqlException e) {
            throw e;
        } catch (SQLException e) {
            throw new DynSqlException("selectOne failed: " + sql, e);
        }
    }

    private long doSelectScalarLong(Connection conn, String sql, Map<String, Object> params) {
        try {
            PreparedStatement ps = cache.get(conn, sql);
            bindDynParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DynSqlException("selectScalar failed: " + sql, e);
        }
    }

    private <T> Optional<T> doSelectScalar(Connection conn, String sql,
                                           Map<String, Object> params, RowMapper<T> mapper) {
        try {
            PreparedStatement ps = cache.get(conn, sql);
            bindDynParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new DynSqlException("selectScalar failed: " + sql, e);
        }
    }

    /**
     * Core insert: uses RETURN_GENERATED_KEYS so caller can retrieve auto-increment PK.
     */
    private SqlResult doInsert(Connection conn, String sql, Map<String, Object> params) {
        try {
            PreparedStatement ps = cache.get(conn, sql, true);
            bindDynParams(ps, params);
            int rows = ps.executeUpdate();
            return new SqlResult(rows, readGeneratedKeys(ps));
        } catch (SQLException e) {
            throw new DynSqlException("insert failed: " + sql, e);
        }
    }

    /**
     * Core write for UPDATE and DELETE — no generated keys needed.
     */
    private SqlResult doWrite(Connection conn, String sql, Map<String, Object> params) {
        try {
            PreparedStatement ps = cache.get(conn, sql, false);
            bindDynParams(ps, params);
            int rows = ps.executeUpdate();
            return new SqlResult(rows, null);
        } catch (SQLException e) {
            throw new DynSqlException("write failed: " + sql, e);
        }
    }

    /**
     * UPDATE batch — optionally manages its own mini-transaction.
     * When {@code ownTx=true} the method sets autoCommit=false, commits, and restores.
     * When {@code ownTx=false} the caller's TransactionScope owns commit/rollback.
     */
    private SqlResult doUpdateBatch(Connection conn, List<UpdateStatementProvider> stmts,
                                    boolean ownTx) {
        String sql = stmts.get(0).getUpdateStatement();
        try {
            PreparedStatement ps = cache.get(conn, sql, false);
            boolean prev = conn.getAutoCommit();
            if (ownTx) conn.setAutoCommit(false);
            try {
                for (UpdateStatementProvider stmt : stmts) {
                    ps.clearParameters();
                    bindDynParams(ps, stmt.getParameters());
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                if (ownTx) conn.commit();
                return new SqlResult(counts);
            } catch (SQLException e) {
                if (ownTx) { try { conn.rollback(); } catch (SQLException ignored) {} }
                throw e;
            } finally {
                if (ownTx) conn.setAutoCommit(prev);
            }
        } catch (SQLException e) {
            throw new DynSqlException("updateBatch failed: " + sql, e);
        }
    }

    private SqlResult doRawWrite(Connection conn, String sql, Object[] params) {
        try {
            PreparedStatement ps = cache.get(conn, sql, true);
            bindPositional(ps, params);
            int rows = ps.executeUpdate();
            return new SqlResult(rows, readGeneratedKeys(ps));
        } catch (SQLException e) {
            throw new DynSqlException("rawExecute failed: " + sql, e);
        }
    }

    private SqlResult doRawBatch(Connection conn, String sql,
                                 List<Object[]> paramSets, boolean ownTx) {
        try {
            PreparedStatement ps = cache.get(conn, sql, false);
            boolean prev = conn.getAutoCommit();
            if (ownTx) conn.setAutoCommit(false);
            try {
                for (Object[] params : paramSets) {
                    ps.clearParameters();
                    bindPositional(ps, params);
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                if (ownTx) conn.commit();
                return new SqlResult(counts);
            } catch (SQLException e) {
                if (ownTx) { try { conn.rollback(); } catch (SQLException ignored) {} }
                throw e;
            } finally {
                if (ownTx) conn.setAutoCommit(prev);
            }
        } catch (SQLException e) {
            throw new DynSqlException("rawBatch failed: " + sql, e);
        }
    }

    // =========================================================================
    // Parameter binding — direct instanceof, no reflection
    // =========================================================================

    /**
     * Bind MyBatis Dynamic SQL named parameters to positional JDBC {@code ?}.
     *
     * MyBatis Dynamic SQL's {@code getParameters()} always returns a
     * {@link java.util.LinkedHashMap} with entries in the SAME ORDER as
     * the {@code ?} placeholders in the rendered SQL — safe to iterate by value.
     */
    private static void bindDynParams(PreparedStatement ps,
                                      Map<String, Object> params) throws SQLException {
        ps.clearParameters();
        int i = 1;
        for (Object val : params.values()) setParam(ps, i++, val);
    }

    private static void bindPositional(PreparedStatement ps, Object[] params) throws SQLException {
        ps.clearParameters();
        for (int i = 0; i < params.length; i++) setParam(ps, i + 1, params[i]);
    }

    /**
     * Direct type dispatch — ordered by frequency. Zero reflection.
     * Falls back to {@code setObject} only for JDBC-native exotic types.
     */
    private static void setParam(PreparedStatement ps, int i, Object v) throws SQLException {
        if (v == null)                                { ps.setNull(i, java.sql.Types.NULL);                                        return; }
        if (v instanceof String)                      { ps.setString(i, (String) v);                                               return; }
        if (v instanceof Long)                        { ps.setLong(i, (Long) v);                                                   return; }
        if (v instanceof Integer)                     { ps.setInt(i, (Integer) v);                                                 return; }
        if (v instanceof Boolean)                     { ps.setBoolean(i, (Boolean) v);                                             return; }
        if (v instanceof java.time.LocalDateTime)     { ps.setTimestamp(i, java.sql.Timestamp.valueOf((java.time.LocalDateTime) v)); return; }
        if (v instanceof java.time.LocalDate)         { ps.setDate(i, java.sql.Date.valueOf((java.time.LocalDate) v));             return; }
        if (v instanceof java.time.Instant)           { ps.setTimestamp(i, java.sql.Timestamp.from((java.time.Instant) v));       return; }
        if (v instanceof java.time.LocalTime)         { ps.setTime(i, java.sql.Time.valueOf((java.time.LocalTime) v));             return; }
        if (v instanceof Double)                      { ps.setDouble(i, (Double) v);                                               return; }
        if (v instanceof Float)                       { ps.setFloat(i, (Float) v);                                                 return; }
        if (v instanceof java.math.BigDecimal)        { ps.setBigDecimal(i, (java.math.BigDecimal) v);                             return; }
        if (v instanceof Short)                       { ps.setShort(i, (Short) v);                                                 return; }
        if (v instanceof byte[])                      { ps.setBytes(i, (byte[]) v);                                               return; }
        if (v instanceof java.util.UUID)              { ps.setString(i, v.toString());                                             return; }
        if (v instanceof Enum<?>)                     { ps.setString(i, ((Enum<?>) v).name());                                     return; }
        if (v instanceof java.sql.Timestamp)          { ps.setTimestamp(i, (java.sql.Timestamp) v);                               return; }
        if (v instanceof java.sql.Date)               { ps.setDate(i, (java.sql.Date) v);                                         return; }
        if (v instanceof java.sql.Time)               { ps.setTime(i, (java.sql.Time) v);                                         return; }
        ps.setObject(i, v);
    }

    private static List<Object> readGeneratedKeys(PreparedStatement ps) throws SQLException {
        List<Object> keys = new ArrayList<>();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            while (rs.next()) keys.add(rs.getObject(1));
        } catch (SQLException ignored) {}
        return keys;
    }

    // =========================================================================
    // Exception
    // =========================================================================

    public static final class DynSqlException extends RuntimeException {
        public DynSqlException(String msg)                  { super(msg); }
        public DynSqlException(String msg, Throwable cause) { super(msg, cause); }
    }
}
