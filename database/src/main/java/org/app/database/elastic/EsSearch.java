package org.app.database.elastic;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent search builder for Elasticsearch 7.x.
 * <p>
 * Every method returns {@code this} for chaining. Call {@link #search(Class)} to execute.
 *
 * <pre>{@code
 * // Simple match
 * EsResult<User> r = EsSearch.on(ops)
 *     .index("users")
 *     .match("name", "alice")
 *     .size(10)
 *     .search(User.class);
 *
 * // Boolean query
 * EsResult<Product> r = EsSearch.on(ops)
 *     .index("products")
 *     .must(q -> q.matchQuery("name", "laptop"))
 *     .filter(q -> q.rangeQuery("price").lte(1000))
 *     .mustNot(q -> q.termQuery("status", "DISCONTINUED"))
 *     .sort("price", false)         // false = DESC
 *     .page(0, 20)
 *     .search(Product.class);
 *
 * // Aggregations
 * EsResult<Map> r = EsSearch.on(ops)
 *     .index("orders")
 *     .aggTerms("by_status", "status", 10)
 *     .aggSum("total_revenue", "amount")
 *     .size(0)                      // no hits, only aggs
 *     .searchRaw();
 *
 * // Scroll (large exports)
 * EsResult<User> page1 = EsSearch.on(ops).index("users").matchAll().scroll("1m").search(User.class);
 * EsResult<User> page2 = EsSearch.on(ops).nextScroll(page1.scrollId(), "1m", User.class);
 * }</pre>
 */
public final class EsSearch {

    private final EsOps ops;

    private String[] indices;
    private final SearchSourceBuilder source = new SearchSourceBuilder();

    // Boolean query parts
    private final List<QueryBuilder> mustClauses    = new ArrayList<>();
    private final List<QueryBuilder> filterClauses  = new ArrayList<>();
    private final List<QueryBuilder> shouldClauses  = new ArrayList<>();
    private final List<QueryBuilder> mustNotClauses = new ArrayList<>();

    // Scroll
    private String scrollTtl;

    private EsSearch(EsOps ops) {
        this.ops = ops;
        source.size(10); // default page size
    }

    public static EsSearch on(EsOps ops) { return new EsSearch(ops); }

    // -------------------------------------------------------------------------
    // Index
    // -------------------------------------------------------------------------

    public EsSearch index(String... indices) {
        this.indices = indices;
        return this;
    }

    // -------------------------------------------------------------------------
    // Simple query shortcuts (no bool wrapper needed for single-query searches)
    // -------------------------------------------------------------------------

    /** match_all */
    public EsSearch matchAll() {
        mustClauses.add(QueryBuilders.matchAllQuery());
        return this;
    }

    /** Single-field full-text match */
    public EsSearch match(String field, Object value) {
        mustClauses.add(QueryBuilders.matchQuery(field, value));
        return this;
    }

    /** match with operator AND/OR */
    public EsSearch match(String field, Object value, Operator operator) {
        mustClauses.add(QueryBuilders.matchQuery(field, value).operator(operator));
        return this;
    }

    /** multi_match across several fields */
    public EsSearch multiMatch(String text, String... fields) {
        mustClauses.add(QueryBuilders.multiMatchQuery(text, fields));
        return this;
    }

    /** Exact term (keyword, id, enum) */
    public EsSearch term(String field, Object value) {
        mustClauses.add(QueryBuilders.termQuery(field, value));
        return this;
    }

    /** terms (IN list) */
    public EsSearch terms(String field, Object... values) {
        mustClauses.add(QueryBuilders.termsQuery(field, values));
        return this;
    }

    /** Prefix */
    public EsSearch prefix(String field, String prefix) {
        mustClauses.add(QueryBuilders.prefixQuery(field, prefix));
        return this;
    }

    /** Wildcard — use sparingly (slow on large indices) */
    public EsSearch wildcard(String field, String pattern) {
        mustClauses.add(QueryBuilders.wildcardQuery(field, pattern));
        return this;
    }

    /** match_phrase for exact phrase in order */
    public EsSearch phrase(String field, String phrase) {
        mustClauses.add(QueryBuilders.matchPhraseQuery(field, phrase));
        return this;
    }

    /** ids query */
    public EsSearch ids(String... ids) {
        mustClauses.add(QueryBuilders.idsQuery().addIds(ids));
        return this;
    }

    /** Range query with fluent from/to — use RangeBuilder */
    public EsSearch range(String field, Consumer<RangeQueryBuilder> configure) {
        RangeQueryBuilder rq = QueryBuilders.rangeQuery(field);
        configure.accept(rq);
        mustClauses.add(rq);
        return this;
    }

    /** Shorthand: range field gte/lte */
    public EsSearch range(String field, Object gte, Object lte) {
        RangeQueryBuilder rq = QueryBuilders.rangeQuery(field);
        if (gte != null) rq.gte(gte);
        if (lte != null) rq.lte(lte);
        mustClauses.add(rq);
        return this;
    }

    /** exists (field is not null) */
    public EsSearch exists(String field) {
        mustClauses.add(QueryBuilders.existsQuery(field));
        return this;
    }

    /** Raw query builder — escape hatch for complex queries */
    public EsSearch rawQuery(QueryBuilder query) {
        mustClauses.add(query);
        return this;
    }

    // -------------------------------------------------------------------------
    // Bool DSL
    // -------------------------------------------------------------------------

    public EsSearch must(QueryBuilder q)    { mustClauses.add(q);    return this; }
    public EsSearch filter(QueryBuilder q)  { filterClauses.add(q);  return this; }
    public EsSearch should(QueryBuilder q)  { shouldClauses.add(q);  return this; }
    public EsSearch mustNot(QueryBuilder q) { mustNotClauses.add(q); return this; }

    /** Convenience: build QueryBuilder inline with lambda */
    public EsSearch must(java.util.function.Function<QueryBuilders, QueryBuilder> fn) {
        return must(fn.apply(null));
    }

    /** Add a nested bool — e.g. OR block inside AND */
    public EsSearch boolShould(Consumer<EsSearch> configure) {
        EsSearch sub = EsSearch.on(ops);
        configure.accept(sub);
        BoolQueryBuilder b = QueryBuilders.boolQuery();
        sub.shouldClauses.forEach(b::should);
        sub.mustClauses.forEach(b::must);
        sub.filterClauses.forEach(b::filter);
        mustClauses.add(b);
        return this;
    }

    // -------------------------------------------------------------------------
    // Sorting
    // -------------------------------------------------------------------------

    /** Sort by field. asc=true for ascending. */
    public EsSearch sort(String field, boolean asc) {
        source.sort(new FieldSortBuilder(field).order(asc ? SortOrder.ASC : SortOrder.DESC));
        return this;
    }

    /** Sort by _score desc (default ES behavior) */
    public EsSearch sortByScore() {
        source.sort(new FieldSortBuilder("_score").order(SortOrder.DESC));
        return this;
    }

    // -------------------------------------------------------------------------
    // Pagination
    // -------------------------------------------------------------------------

    /** @param from  offset (0-based), @param size page size */
    public EsSearch page(int from, int size) {
        source.from(from).size(size);
        return this;
    }

    public EsSearch size(int size) { source.size(size); return this; }
    public EsSearch from(int from) { source.from(from); return this; }

    // -------------------------------------------------------------------------
    // Source filtering
    // -------------------------------------------------------------------------

    /** Include only specific fields in response */
    public EsSearch includes(String... fields) {
        source.fetchSource(fields, null);
        return this;
    }

    /** Exclude fields from response */
    public EsSearch excludes(String... fields) {
        source.fetchSource(null, fields);
        return this;
    }

    /** Don't return source at all (useful when only ids/aggs needed) */
    public EsSearch noSource() {
        source.fetchSource(false);
        return this;
    }

    // -------------------------------------------------------------------------
    // Scroll (cursor pagination for large exports)
    // -------------------------------------------------------------------------

    /** Enable scroll context. Use "1m", "5m" etc. */
    public EsSearch scroll(String keepAliveDuration) {
        this.scrollTtl = keepAliveDuration;
        return this;
    }

    // -------------------------------------------------------------------------
    // Aggregations
    // -------------------------------------------------------------------------

    /** Terms aggregation — top N buckets by field */
    public EsSearch aggTerms(String aggName, String field, int size) {
        source.aggregation(AggregationBuilders.terms(aggName).field(field).size(size));
        return this;
    }

    /** Date histogram aggregation */
    public EsSearch aggDateHistogram(String aggName, String field, String calendarInterval) {
        DateHistogramAggregationBuilder agg = AggregationBuilders.dateHistogram(aggName)
            .field(field)
            .calendarInterval(new DateHistogramInterval(calendarInterval));
        source.aggregation(agg);
        return this;
    }

    /** Sum aggregation */
    public EsSearch aggSum(String aggName, String field) {
        source.aggregation(AggregationBuilders.sum(aggName).field(field));
        return this;
    }

    /** Avg aggregation */
    public EsSearch aggAvg(String aggName, String field) {
        source.aggregation(AggregationBuilders.avg(aggName).field(field));
        return this;
    }

    /** Min aggregation */
    public EsSearch aggMin(String aggName, String field) {
        source.aggregation(AggregationBuilders.min(aggName).field(field));
        return this;
    }

    /** Max aggregation */
    public EsSearch aggMax(String aggName, String field) {
        source.aggregation(AggregationBuilders.max(aggName).field(field));
        return this;
    }

    /** Value count aggregation */
    public EsSearch aggCount(String aggName, String field) {
        source.aggregation(AggregationBuilders.count(aggName).field(field));
        return this;
    }

    /** Cardinality (distinct count) */
    public EsSearch aggCardinality(String aggName, String field) {
        source.aggregation(AggregationBuilders.cardinality(aggName).field(field));
        return this;
    }

    /** Raw aggregation builder — escape hatch */
    public EsSearch agg(org.elasticsearch.search.aggregations.AggregationBuilder agg) {
        source.aggregation(agg);
        return this;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    /**
     * Execute and deserialize hits to {@code type}.
     */
    public <T> EsResult<T> search(Class<T> type) {
        SearchResponse resp = execute();
        EsMapper m = ops.mapper();
        return EsResult.from(resp, src -> m.fromMap(src, type));
    }

    /**
     * Execute and return hits as raw Maps (no deserialization).
     */
    public EsResult<Map<String, Object>> searchRaw() {
        SearchResponse resp = execute();
        return EsResult.from(resp, src -> src);
    }

    /**
     * Fetch next scroll page from a previous {@link EsResult#scrollId()}.
     */
    public <T> EsResult<T> nextScroll(String scrollId, String keepAlive, Class<T> type) {
        try (var client = ops.client()){
            SearchScrollRequest req = new SearchScrollRequest(scrollId)
                .scroll(TimeValue.parseTimeValue(keepAlive, "scroll"));
            SearchResponse resp = client.scroll(req, RequestOptions.DEFAULT);
            EsMapper m = ops.mapper();
            return EsResult.from(resp, src -> m.fromMap(src, type));
        } catch (IOException e) {
            throw new EsOps.EsException("scroll failed", e);
        }
    }

    /**
     * Count only — returns total matching documents without fetching source.
     */
    public long count() {
        source.size(0).fetchSource(false);
        return execute().getHits().getTotalHits().value;
    }

    // -------------------------------------------------------------------------
    // Internal build
    // -------------------------------------------------------------------------

    private SearchResponse execute() {
        buildQuery();
        SearchRequest req = new SearchRequest(indices).source(source);
        if (scrollTtl != null) {
            req.scroll(TimeValue.parseTimeValue(scrollTtl, "scroll"));
        }
        try (var client = ops.client()){
            return client.search(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("search failed on index: " + java.util.Arrays.toString(indices), e);
        }
    }

    private void buildQuery() {
        boolean hasBool = !mustClauses.isEmpty() || !filterClauses.isEmpty()
            || !shouldClauses.isEmpty() || !mustNotClauses.isEmpty();

        if (!hasBool) {
            source.query(QueryBuilders.matchAllQuery());
            return;
        }

        // Single must clause and nothing else → use it directly (simpler query)
        if (mustClauses.size() == 1 && filterClauses.isEmpty()
            && shouldClauses.isEmpty() && mustNotClauses.isEmpty()) {
            source.query(mustClauses.get(0));
            return;
        }

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        mustClauses.forEach(bool::must);
        filterClauses.forEach(bool::filter);
        shouldClauses.forEach(bool::should);
        mustNotClauses.forEach(bool::mustNot);
        source.query(bool);
    }
}
