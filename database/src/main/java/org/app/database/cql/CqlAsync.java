package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Asynchronous CQL operations — all methods return {@link CompletableFuture}.
 * <p>
 * Uses the DataStax driver's non-blocking async API backed by Netty.
 * Ideal for high-throughput write pipelines and reactive service layers.
 *
 * <pre>{@code
 * CqlAsync async = CqlAsync.of(factory);
 *
 * // ── Async select ──────────────────────────────────────────────────────────
 * CompletableFuture&lt;Optional&lt;User&gt;&gt; f = async.selectOne(
 *     "SELECT * FROM users WHERE id = ?",
 *     row -&gt; new User(row.getString("id"), row.getString("name")),
 *     "user-123");
 *
 * // ── Async write ───────────────────────────────────────────────────────────
 * CompletableFuture&lt;Void&gt; w = async.write(
 *     "INSERT INTO events (id, ts, data) VALUES (?, ?, ?)",
 *     uuid, now, payload);
 *
 * // ── Parallel inserts ──────────────────────────────────────────────────────
 * List&lt;CompletableFuture&lt;Void&gt;&gt; futures = users.stream()
 *     .map(u -&gt; async.write(
 *         "INSERT INTO users (id, name) VALUES (?, ?)",
 *         u.id(), u.name()))
 *     .toList();
 * CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
 *
 * // ── Async LWT ─────────────────────────────────────────────────────────────
 * async.insertIfNotExists(
 *     "INSERT INTO users (id, email) VALUES (?, ?) IF NOT EXISTS",
 *     id, email)
 *     .thenAccept(inserted -&gt; log.info("inserted: {}", inserted));
 * }</pre>
 */
public final class CqlAsync {

    private final CqlSession  session;
    private final CqlPrepared prepared;

    private CqlAsync(CqlSession session) {
        this.session  = session;
        this.prepared = CqlPrepared.of(session);
    }

    public static CqlAsync of(CqlSessionFactory factory) {
        return new CqlAsync(factory.session());
    }

    public static CqlAsync of(CqlSession session) {
        return new CqlAsync(session);
    }

    public CqlPrepared prepared() { return prepared; }

    // -------------------------------------------------------------------------
    // Async select
    // -------------------------------------------------------------------------

    /**
     * Async select one row, mapped to T.
     */
    public <T> CompletableFuture<Optional<T>> selectOne(
            String cql, Function<Row, T> mapper, Object... values) {
        return session.executeAsync(prepared.bind(cql, values))
            .toCompletableFuture()
            .thenApply(rs -> Optional.ofNullable(rs.one()).map(mapper));
    }

    public <T> CompletableFuture<Optional<T>> selectOne(
            String cql, Class<T> type, Object... values) {
        return selectOne(cql, row -> CqlMapper.map(row, type), values);
    }

    /**
     * Async select list — fetches ALL pages before returning.
     * For large tables use {@link CqlPaging} instead.
     */
    public <T> CompletableFuture<List<T>> selectList(
            String cql, Function<Row, T> mapper, Object... values) {
        return session.executeAsync(prepared.bind(cql, values))
            .toCompletableFuture()
            .thenCompose(rs -> fetchAll(rs, mapper));
    }

    public <T> CompletableFuture<List<T>> selectList(
            String cql, Class<T> type, Object... values) {
        return selectList(cql, row -> CqlMapper.map(row, type), values);
    }

    /**
     * Async raw row select.
     */
    public CompletableFuture<Optional<Row>> selectRow(String cql, Object... values) {
        return session.executeAsync(prepared.bind(cql, values))
            .toCompletableFuture()
            .thenApply(rs -> Optional.ofNullable(rs.one()));
    }

    // -------------------------------------------------------------------------
    // Async write
    // -------------------------------------------------------------------------

    /**
     * Async execute — returns Void future (fire and wait for ack).
     */
    public CompletableFuture<Void> write(String cql, Object... values) {
        return session.executeAsync(prepared.bind(cql, values))
            .toCompletableFuture()
            .thenApply(rs -> null);
    }

    /**
     * Async execute returning the raw AsyncResultSet (for LWT applied check).
     */
    public CompletableFuture<AsyncResultSet> execute(String cql, Object... values) {
        return session.executeAsync(prepared.bind(cql, values)).toCompletableFuture();
    }

    public CompletableFuture<AsyncResultSet> execute(BoundStatement bs) {
        return session.executeAsync(bs).toCompletableFuture();
    }

    // -------------------------------------------------------------------------
    // Async LWT
    // -------------------------------------------------------------------------

    public CompletableFuture<Boolean> insertIfNotExists(String cql, Object... values) {
        return execute(cql, values).thenApply(rs -> {
            Row row = rs.one();
            return row != null && row.getBoolean("[applied]");
        });
    }

    public CompletableFuture<Boolean> updateIf(String cql, Object... values) {
        return insertIfNotExists(cql, values);
    }

    public CompletableFuture<CqlOps.LwtResult> executeIf(String cql, Object... values) {
        return execute(cql, values).thenApply(rs -> {
            Row row = rs.one();
            if (row == null) return new CqlOps.LwtResult(false, null);
            boolean applied = row.getBoolean("[applied]");
            return new CqlOps.LwtResult(applied, applied ? null : row);
        });
    }

    // -------------------------------------------------------------------------
    // Async count
    // -------------------------------------------------------------------------

    public CompletableFuture<Long> count(String cql, Object... values) {
        return session.executeAsync(prepared.bind(cql, values))
            .toCompletableFuture()
            .thenApply(rs -> {
                Row row = rs.one();
                return row == null ? 0L : row.getLong(0);
            });
    }

    // -------------------------------------------------------------------------
    // Parallel writes (fire all, wait for all)
    // -------------------------------------------------------------------------

    /**
     * Execute multiple independent write statements in parallel.
     * Returns when ALL have been acknowledged by the cluster.
     *
     * <pre>
     * async.writeAll(List.of(
     *     prepared.bind("INSERT INTO a (id) VALUES (?)", id1),
     *     prepared.bind("INSERT INTO b (id) VALUES (?)", id2)
     * )).join();
     * </pre>
     */
    public CompletableFuture<Void> writeAll(List<BoundStatement> statements) {
        return CompletableFuture.allOf(statements.stream()
            .map(bs -> session.executeAsync(bs)
                .toCompletableFuture()
                .<Void>thenApply(r -> null))
            .toArray(CompletableFuture[]::new));
    }

    // -------------------------------------------------------------------------
    // Internal: fetch all pages
    // -------------------------------------------------------------------------

    private <T> CompletionStage<List<T>> fetchAll(AsyncResultSet rs, Function<Row, T> mapper) {
        List<T> acc = new ArrayList<>();
        return fetchPage(rs, mapper, acc);
    }

    private <T> CompletionStage<List<T>> fetchPage(
            AsyncResultSet rs, Function<Row, T> mapper, List<T> acc) {
        for (Row row : rs.currentPage()) acc.add(mapper.apply(row));
        if (rs.hasMorePages()) {
            return rs.fetchNextPage().thenCompose(next -> fetchPage(next, mapper, acc));
        }
        return CompletableFuture.completedFuture(acc);
    }
}
