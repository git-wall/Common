package org.app.database.elastic;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.core.TimeValue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Cursor-based scroll iterator for large result sets in Elasticsearch 7.x.
 * <p>
 * Automatically handles scroll context lifecycle. Always close after use.
 *
 * <pre>
 * // Iterate all users in batches of 500
 * try (EsScroll&lt;User&gt; scroll = EsScroll.of(ops)
 *         .index("users")
 *         .match("status", "ACTIVE")
 *         .batchSize(500)
 *         .keepAlive("2m")
 *         .open(User.class)) {
 *
 *     scroll.forEachBatch(batch -> {
 *         batchProcessor.process(batch);
 *     });
 * }
 *
 * // Or iterate page by page
 * try (EsScroll&lt;User&gt; scroll = EsScroll.of(ops)
 *         .index("users").matchAll().open(User.class)) {
 *     while (scroll.hasNext()) {
 *         List&lt;User&gt; page = scroll.next();
 *         process(page);
 *     }
 * }
 * </pre>
 *
 * @param <T> document type
 */
public final class EsScroll<T> implements Closeable, Iterator<List<T>> {

    private final EsOps     ops;
    private final EsMapper  mapper;
    private final Class<T>  type;
    private final String    keepAlive;

    private String  scrollId;
    private List<T> nextBatch;
    private boolean exhausted = false;

    private EsScroll(EsOps ops, Class<T> type, EsResult<T> firstPage, String keepAlive) {
        this.ops       = ops;
        this.mapper    = ops.mapper();
        this.type      = type;
        this.keepAlive = keepAlive;
        this.scrollId  = firstPage.scrollId();
        this.nextBatch = firstPage.isEmpty() ? null : firstPage.hits();
        if (firstPage.isEmpty()) this.exhausted = true;
    }

    // -------------------------------------------------------------------------
    // Builder entry point
    // -------------------------------------------------------------------------

    public static Builder builder(EsOps ops) { return new Builder(ops); }

    /** Shorthand — returns a Builder */
    public static Builder of(EsOps ops) { return new Builder(ops); }

    // -------------------------------------------------------------------------
    // Iterator
    // -------------------------------------------------------------------------

    @Override
    public boolean hasNext() {
        return !exhausted && nextBatch != null && !nextBatch.isEmpty();
    }

    @Override
    public List<T> next() {
        if (!hasNext()) throw new NoSuchElementException("No more scroll pages");
        List<T> current = nextBatch;
        fetchNextPage();
        return current;
    }

    /**
     * Consume all pages batch by batch.
     * <pre>
     * scroll.forEachBatch(batch -> saveToCsv(batch));
     * </pre>
     */
    public void forEachBatch(Consumer<List<T>> consumer) {
        while (hasNext()) {
            consumer.accept(next());
        }
    }

    /**
     * Consume all documents one by one.
     */
    public void forEach(Consumer<T> consumer) {
        forEachBatch(batch -> batch.forEach(consumer));
    }

    // -------------------------------------------------------------------------

    private void fetchNextPage() {
        if (exhausted || scrollId == null) {
            exhausted = true;
            nextBatch = null;
            return;
        }
        try (var client = ops.client()){
            var resp = client.scroll(
                new SearchScrollRequest(scrollId)
                    .scroll(TimeValue.parseTimeValue(keepAlive, "scroll")),
                RequestOptions.DEFAULT
            );
            scrollId = resp.getScrollId();
            List<T> batch = java.util.Arrays.stream(resp.getHits().getHits())
                .map(h -> mapper.fromMap(h.getSourceAsMap(), type))
                .collect(Collectors.toList());

            if (batch.isEmpty()) {
                exhausted = true;
                nextBatch = null;
            } else {
                nextBatch = batch;
            }
        } catch (IOException e) {
            exhausted = true;
            throw new EsOps.EsException("scroll fetch failed", e);
        }
    }

    @Override
    public void close() {
        if (scrollId == null) return;
        try (var client = ops.client()){
            ClearScrollRequest req = new ClearScrollRequest();
            req.addScrollId(scrollId);
            client.clearScroll(req, RequestOptions.DEFAULT);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static final class Builder {
        private final EsOps ops;
        private final EsSearch    search;
        private int         batchSize = 200;
        private String      keepAlive = "2m";

        Builder(EsOps ops) {
            this.ops    = ops;
            this.search = EsSearch.on(ops);
        }

        public Builder index(String... indices) { search.index(indices); return this; }
        public Builder term(String f, Object v) { search.term(f, v);   return this; }
        public Builder match(String f, Object v) { search.match(f, v);   return this; }
        public Builder matchAll()                { search.matchAll();      return this; }
        public Builder filter(org.elasticsearch.index.query.QueryBuilder q) { search.filter(q); return this; }
        public Builder sort(String field, boolean asc) { search.sort(field, asc); return this; }
        public Builder includes(String... fields) { search.includes(fields); return this; }
        public Builder batchSize(int size)        { this.batchSize = size; return this; }
        public Builder keepAlive(String ttl)      { this.keepAlive = ttl;  return this; }

        /** Open the scroll context and return the ready-to-iterate EsScroll. */
        public <T> EsScroll<T> open(Class<T> type) {
            EsResult<T> firstPage = search.size(batchSize).scroll(keepAlive).search(type);
            return new EsScroll<>(ops, type, firstPage, keepAlive);
        }
    }
}
