package org.app.database.mybatis;

import java.util.Collections;
import java.util.List;

/**
 * Result of an INSERT, UPDATE, or DELETE operation.
 *
 * <pre>
 * SqlResult r = db.insert(stmt);
 * r.rowsAffected()          // 1
 * r.generatedKey(Long.class) // auto-increment PK
 *
 * SqlResult batch = db.insertBatch(stmts);
 * batch.rowsAffected()      // total rows across all batches
 * batch.batchCounts()       // per-statement counts
 * </pre>
 */
public final class SqlResult {

    private final int        rowsAffected;
    private final List<Object> generatedKeys;
    private final int[]      batchCounts;

    /** Single-statement result with generated keys. */
    public SqlResult(int rowsAffected, List<Object> generatedKeys) {
        this.rowsAffected  = rowsAffected;
        this.generatedKeys = generatedKeys != null
            ? Collections.unmodifiableList(generatedKeys)
            : Collections.emptyList();
        this.batchCounts   = new int[0];
    }

    /** Batch result. */
    public SqlResult(int[] batchCounts) {
        int total = 0;
        for (int c : batchCounts) {
            if (c >= 0) total += c;
        }
        this.rowsAffected  = total;
        this.generatedKeys = Collections.emptyList();
        this.batchCounts   = batchCounts.clone();
    }

    /** Total rows affected. */
    public int rowsAffected()   { return rowsAffected; }

    /** True if at least one row was affected. */
    public boolean affected()   { return rowsAffected > 0; }

    /** Generated keys (auto-increment / sequences). May be empty. */
    public List<Object> generatedKeys() { return generatedKeys; }

    /**
     * First generated key cast to the given type.
     * Common use: {@code result.generatedKey(Long.class)} for auto-increment PKs.
     */
    public <K> K generatedKey(Class<K> type) {
        if (generatedKeys.isEmpty()) return null;
        Object key = generatedKeys.get(0);
        // Numeric coercion — JDBC returns different types per driver
        if (type == Long.class && key instanceof Number) {
            return type.cast(((Number) key).longValue());
        }
        if (type == Integer.class && key instanceof Number) {
            return type.cast(((Number) key).intValue());
        }
        return type.cast(key);
    }

    /** Per-statement row counts for batch operations. */
    public int[] batchCounts()  { return batchCounts.clone(); }

    @Override
    public String toString() {
        return String.format("SqlResult{rowsAffected=%d, generatedKeys=%s}",
            rowsAffected, generatedKeys);
    }
}
