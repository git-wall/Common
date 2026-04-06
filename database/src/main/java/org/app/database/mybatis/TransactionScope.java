package org.app.database.mybatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Lightweight transaction scope backed by a single JDBC {@link Connection}.
 * <p>
 * Use with try-with-resources — auto-rolls back on any exception.
 * Call {@link #commit()} explicitly to persist changes.
 *
 * <pre>
 * try (TransactionScope tx = db.transaction()) {
 *
 *     db.insert(tx, insertUserStmt);
 *     db.insert(tx, insertAuditStmt);
 *
 *     tx.commit();   // only committed if both succeed
 * }
 * // auto-rollback if commit() was never called or an exception was thrown
 *
 * // Savepoints
 * try (TransactionScope tx = db.transaction()) {
 *     Savepoint sp = tx.savepoint("before_risky");
 *     try {
 *         db.execute(tx, riskyStmt);
 *     } catch (Exception e) {
 *         tx.rollbackTo(sp);
 *     }
 *     tx.commit();
 * }
 * </pre>
 */
public final class TransactionScope implements AutoCloseable {

    private final Connection conn;
    private final boolean    wasAutoCommit;
    private boolean          committed = false;
    private boolean          closed    = false;

    TransactionScope(Connection conn) throws SQLException {
        this.conn          = conn;
        this.wasAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
    }

    /** The underlying connection — pass to {@link DynSql} execute methods. */
    public Connection connection() { return conn; }

    /** Commit all changes in this transaction scope. */
    public void commit() throws SQLException {
        conn.commit();
        committed = true;
    }

    /** Explicit rollback (also called automatically on close if not committed). */
    public void rollback() throws SQLException {
        conn.rollback();
    }

    /** Create a savepoint with a given name. */
    public Savepoint savepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    /** Roll back to a savepoint without rolling back the entire transaction. */
    public void rollbackTo(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    /** Release a savepoint (frees server resources). */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    public boolean isCommitted() { return committed; }
    public boolean isClosed()    { return closed; }

    @Override
    public void close() throws SQLException {
        if (closed) return;
        closed = true;
        try {
            if (!committed) {
                try { conn.rollback(); }
                catch (SQLException ignored) {}
            }
        } finally {
            try { conn.setAutoCommit(wasAutoCommit); }
            catch (SQLException ignored) {}
            conn.close(); // return to pool
        }
    }
}
