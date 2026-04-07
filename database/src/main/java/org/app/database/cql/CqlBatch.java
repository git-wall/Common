package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Batch statement builder for CQL.
 * <p>
 * <b>Batch types:</b>
 * <ul>
 *   <li> — atomicity guaranteed. If any statement fails, ALL are retried.
 *       Use for multi-partition writes that MUST be atomic. Slower — avoid for high throughput.</li>
 *   <li> — no atomicity guarantee. Faster. Use only when statements
 *       target the SAME partition key (natural grouping), purely as a network optimization.</li>
 *   <li> — required for {@code COUNTER} column updates (Cassandra rule:
 *       counter batches cannot mix counter and non-counter tables).</li>
 * </ul>
 *
 * <b>Warning:</b> Large batches cause coordinator hotspots. Prefer unlogged batches
 * of the same partition, or use async parallel writes ({@link CqlAsync#writeAll})
 * for multi-partition scenarios.
 *
 * <pre>{@code
 * CqlBatch batch = CqlBatch.logged(factory);
 *
 * batch
 *     .add("INSERT INTO users (id, name) VALUES (?, ?)",        userId, name)
 *     .add("INSERT INTO user_by_email (email, id) VALUES (?,?)", email, userId)
 *     .add("INSERT INTO audit (id, action) VALUES (?, ?)",       auditId, "CREATE")
 *     .execute();
 *
 * // Async
 * batch.executeAsync().thenRun(() -> log.info("Batch done"));
 *
 * // Reusable — clear and reuse
 * batch.clear();
 * batch.add(...).add(...).execute();
 * }</pre>
 */
public final class CqlBatch {

    public enum Type { LOGGED, UNLOGGED, COUNTER }

    private final CqlSession  session;
    private final CqlPrepared prepared;
    private final Type        type;
    private final List<BatchableStatement<?>> statements = new ArrayList<>();

    private CqlBatch(CqlSession session, Type type) {
        this.session  = session;
        this.prepared = CqlPrepared.of(session);
        this.type     = type;
    }

    // ── Factories ────────────────────────────────────────────────────────────

    /** Logged batch — atomic, all-or-nothing across partitions. */
    public static CqlBatch logged(CqlSessionFactory factory) {
        return new CqlBatch(factory.session(), Type.LOGGED);
    }

    /**
     * Unlogged batch — no atomicity, no retry log.
     * Best for grouping writes to the same partition key.
     */
    public static CqlBatch unlogged(CqlSessionFactory factory) {
        return new CqlBatch(factory.session(), Type.UNLOGGED);
    }

    /**
     * Counter batch — required for COUNTER column increments.
     * Cannot mix counter and non-counter statements.
     */
    public static CqlBatch counter(CqlSessionFactory factory) {
        return new CqlBatch(factory.session(), Type.COUNTER);
    }

    public static CqlBatch of(CqlSessionFactory factory, Type type) {
        return new CqlBatch(factory.session(), type);
    }

    // ── Add statements ────────────────────────────────────────────────────────

    /**
     * Add a prepared CQL statement with bind values.
     */
    public CqlBatch add(String cql, Object... values) {
        statements.add(prepared.bind(cql, values));
        return this;
    }

    /**
     * Add a pre-built {@link BoundStatement} directly.
     */
    public CqlBatch add(BoundStatement bs) {
        statements.add(bs);
        return this;
    }

    /**
     * Add a {@link SimpleStatement} (for DDL or when prepared is not needed).
     */
    public CqlBatch add(SimpleStatement stmt) {
        statements.add(stmt);
        return this;
    }

    // ── Execute ───────────────────────────────────────────────────────────────

    /**
     * Execute the batch synchronously.
     *
     * @throws IllegalStateException if batch is empty
     */
    public void execute() {
        if (statements.isEmpty()) throw new IllegalStateException("Batch is empty");
        session.execute(build());
    }

    /**
     * Execute asynchronously.
     */
    public CompletableFuture<Void> executeAsync() {
        if (statements.isEmpty()) return CompletableFuture.completedFuture(null);
        return session.executeAsync(build()).toCompletableFuture().thenApply(rs -> null);
    }

    // ── Introspection ─────────────────────────────────────────────────────────

    public int size()      { return statements.size(); }
    public boolean isEmpty(){ return statements.isEmpty(); }
    public Type type()     { return type; }

    /**
     * Clear all buffered statements (reuse the same CqlBatch instance).
     */
    public CqlBatch clear() {
        statements.clear();
        return this;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private BatchStatement build() {
        BatchType batchType;
        switch (type) {
            case LOGGED:   batchType = DefaultBatchType.LOGGED;   break;
            case COUNTER:  batchType = DefaultBatchType.COUNTER;  break;
            default:       batchType = DefaultBatchType.UNLOGGED; break;
        }
        return BatchStatement.newInstance(batchType)
            .addAll(statements);
    }
}
