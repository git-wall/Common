package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cursor-based paging for large Cassandra/ScyllaDB result sets.
 * <p>
 * <b>Never use {@code LIMIT} + {@code OFFSET}</b> in Cassandra — there is no
 * efficient row offset. Instead, use the driver's built-in paging state (a token
 * cursor) to continue from where you left off.
 *
 * <pre>
 * CqlPaging paging = CqlPaging.of(factory);
 *
 * // ── Iterate all rows in batches of 500 ────────────────────────────────────
 * paging.forEach(
 *     "SELECT * FROM events WHERE user_id = ?",
 *     row -&gt; new Event(row),
 *     500,
 *     batch -&gt; processBatch(batch),
 *     userId);
 *
 * // ── Paginate for API responses (page token) ───────────────────────────────
 * Page&lt;User&gt; page1 = paging.page(
 *     "SELECT * FROM users",
 *     row -&gt; new User(row),
 *     100,           // page size
 *     null);         // no cursor = first page
 *
 * String cursor = page1.nextCursor(); // send to client
 *
 * Page&lt;User&gt; page2 = paging.page(
 *     "SELECT * FROM users",
 *     row -&gt; new User(row),
 *     100,
 *     cursor);        // client sends back
 *
 * // ── Async forEach ────────────────────────────────────────────────────────
 * paging.forEachAsync(
 *     "SELECT * FROM events WHERE user_id = ?",
 *     row -&gt; new Event(row),
 *     500,
 *     batch -&gt; writeToCsv(batch),
 *     userId).join();
 * </pre>
 */
public final class CqlPaging {

    private final CqlSession  session;
    private final CqlPrepared prepared;

    private CqlPaging(CqlSession session) {
        this.session  = session;
        this.prepared = CqlPrepared.of(session);
    }

    public static CqlPaging of(CqlSessionFactory factory) {
        return new CqlPaging(factory.session());
    }

    // -------------------------------------------------------------------------
    // forEach — process all rows in batches (full table scan safe)
    // -------------------------------------------------------------------------

    /**
     * Iterate ALL rows matching the query, calling {@code batchHandler} for each
     * page of {@code pageSize} rows. Safe for full-table exports — never loads
     * the entire table into memory.
     *
     * @param cql          CQL with ? placeholders
     * @param mapper       Row → T function
     * @param pageSize     rows per batch (driver fetch size)
     * @param batchHandler called once per page
     * @param values       bind values
     */
    public <T> void forEach(String cql, Function<Row, T> mapper,
                            int pageSize, Consumer<List<T>> batchHandler,
                            Object... values) {
        Statement<?> stmt = prepared.bind(cql, values).setPageSize(pageSize);
        ResultSet rs = session.execute(stmt);

        List<T> batch = new ArrayList<>(pageSize);
        int available = rs.getAvailableWithoutFetching();

        for (Row row : rs) {
            batch.add(mapper.apply(row));
            // When we've consumed all rows on this page, flush and reset
            if ((batch.size() >= pageSize || --available == 0)
                && (rs.isFullyFetched() || batch.size() >= pageSize)) {
                    batchHandler.accept(new ArrayList<>(batch));
                    batch.clear();
                    available = rs.getAvailableWithoutFetching();
                }

        }
        if (!batch.isEmpty()) batchHandler.accept(batch);
    }

    /** forEach with annotation-based mapping. */
    public <T> void forEach(String cql, Class<T> type,
                            int pageSize, Consumer<List<T>> batchHandler,
                            Object... values) {
        forEach(cql, row -> CqlMapper.map(row, type), pageSize, batchHandler, values);
    }

    // -------------------------------------------------------------------------
    // Page — API-style pagination with opaque cursor token
    // -------------------------------------------------------------------------

    /**
     * Fetch one page of results. Pass {@code null} cursor for the first page.
     * Returns a {@link Page} containing the rows and a cursor for the next page.
     *
     * <p>The cursor is a Base64-encoded paging state — it can be safely serialized
     * to a string and sent to clients for stateless pagination.
     *
     * @param cql      CQL query
     * @param mapper   Row → T
     * @param pageSize max rows per page
     * @param cursor   null for first page, or value from previous {@link Page#nextCursor()}
     */
    public <T> Page<T> page(String cql, Function<Row, T> mapper,
                             int pageSize, String cursor,
                             Object... values) {
        BoundStatement bs = prepared.bind(cql, values).setPageSize(pageSize);

        if (cursor != null) {
            ByteBuffer pagingState = ByteBuffer.wrap(
                java.util.Base64.getDecoder().decode(cursor));
            bs = bs.setPagingState(pagingState);
        }

        ResultSet rs   = session.execute(bs);
        List<T>   rows = new ArrayList<>(pageSize);

        // Consume only the current page (not more)
        int remaining = rs.getAvailableWithoutFetching();
        for (int i = 0; i < remaining; i++) {
            Row row = rs.one();
            if (row == null) break;
            rows.add(mapper.apply(row));
        }

        // Build cursor for next page
        String nextCursor;
        PagingState pagingState = rs.getExecutionInfo().getSafePagingState();
        if (pagingState == null) {
            nextCursor = null;
        } else {
            ByteBuffer nextState = pagingState.getRawPagingState();
            nextCursor = java.util.Base64.getEncoder().encodeToString(toByteArray(nextState));
        }

        return new Page<>(rows, nextCursor, cursor == null);
    }

    public <T> Page<T> page(String cql, Class<T> type,
                             int pageSize, String cursor, Object... values) {
        return page(cql, row -> CqlMapper.map(row, type), pageSize, cursor, values);
    }

    // -------------------------------------------------------------------------
    // Async forEach
    // -------------------------------------------------------------------------

    public <T> CompletableFuture<Void> forEachAsync(
            String cql, Function<Row, T> mapper,
            int pageSize, Consumer<List<T>> batchHandler,
            Object... values) {
        BoundStatement bs = prepared.bind(cql, values).setPageSize(pageSize);
        return session.executeAsync(bs)
            .toCompletableFuture()
            .thenCompose(rs -> processAsync(rs, mapper, batchHandler, new ArrayList<>(pageSize)));
    }

    private <T> CompletableFuture<Void> processAsync(
        AsyncResultSet rs, Function<Row, T> mapper,
        Consumer<List<T>> handler, List<T> accumulator) {

        for (Row row : rs.currentPage()) accumulator.add(mapper.apply(row));

        if (rs.hasMorePages()) {
            handler.accept(new ArrayList<>(accumulator));
            accumulator.clear();
            return rs.fetchNextPage()
                .toCompletableFuture()
                .thenCompose(next -> processAsync(next, mapper, handler, accumulator));
        }
        if (!accumulator.isEmpty()) handler.accept(accumulator);
        return CompletableFuture.completedFuture(null);
    }

    // -------------------------------------------------------------------------
    // Iterator style
    // -------------------------------------------------------------------------

    /**
     * Return a lazy {@link Iterator} that fetches pages on demand.
     * Use with try-with-resources pattern or drain fully.
     */
    public <T> Iterator<T> iterator(String cql, Function<Row, T> mapper,
                                     int pageSize, Object... values) {
        Statement<?> stmt  = prepared.bind(cql, values).setPageSize(pageSize);
        ResultSet    rs    = session.execute(stmt);
        Iterator<Row> iter = rs.iterator();
        return new Iterator<T>() {
            @Override public boolean hasNext() { return iter.hasNext(); }
            @Override public T next()          { return mapper.apply(iter.next()); }
        };
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private static byte[] toByteArray(ByteBuffer bb) {
        byte[] bytes = new byte[bb.remaining()];
        bb.duplicate().get(bytes);
        return bytes;
    }

    // -------------------------------------------------------------------------
    // Page result type
    // -------------------------------------------------------------------------

    /**
     * One page of results with cursor for next page.
     * {@code nextCursor} is null if this is the last page.
     */
    public static final class Page<T> {

        private final List<T>  rows;
        private final String   nextCursor;
        private final boolean  firstPage;

        public Page(List<T> rows, String nextCursor, boolean firstPage) {
            this.rows       = rows;
            this.nextCursor = nextCursor;
            this.firstPage  = firstPage;
        }

        public List<T> rows()        { return rows; }
        public String  nextCursor()  { return nextCursor; }
        public boolean firstPage()   { return firstPage; }

        /** True if there are more pages after this one. */
        public boolean hasNext()  { return nextCursor != null; }
        public boolean isLast()   { return nextCursor == null; }
        public int     size()     { return rows.size(); }
        public boolean isEmpty()  { return rows.isEmpty(); }
    }
}
