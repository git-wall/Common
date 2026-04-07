package org.app.database.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Typed wrapper around ES {@link SearchResponse}.
 * Provides convenient access to hits, total count, aggregations, and scroll id.
 *
 * <pre>
 * EsResult&lt;User&gt; result = EsSearch.on(ops).index("users").match("name","alice").search(User.class);
 * long total = result.total();
 * List&lt;User&gt; users = result.hits();
 * String scrollId = result.scrollId(); // for pagination
 * </pre>
 *
 * @param <T> document type
 */
public final class EsResult<T> {

    private final long         total;
    private final List<T>      hits;
    private final List<String> ids;
    private final List<Float>  scores;
    private final Aggregations aggregations;
    private final String       scrollId;
    private final float        maxScore;

    private EsResult(long total, List<T> hits, List<String> ids,
                     List<Float> scores, Aggregations aggs,
                     String scrollId, float maxScore) {
        this.total        = total;
        this.hits         = hits;
        this.ids          = ids;
        this.scores       = scores;
        this.aggregations = aggs;
        this.scrollId     = scrollId;
        this.maxScore     = maxScore;
    }

    static <T> EsResult<T> from(SearchResponse resp, Function<Map<String, Object>, T> mapper) {
        SearchHit[] rawHits = resp.getHits().getHits();

        List<T>      hits   = Arrays.stream(rawHits).map(h -> mapper.apply(h.getSourceAsMap())).collect(Collectors.toList());
        List<String> ids    = Arrays.stream(rawHits).map(SearchHit::getId).collect(Collectors.toList());
        List<Float>  scores = Arrays.stream(rawHits).map(SearchHit::getScore).collect(Collectors.toList());

        return new EsResult<>(
            resp.getHits().getTotalHits().value,
            Collections.unmodifiableList(hits),
            Collections.unmodifiableList(ids),
            Collections.unmodifiableList(scores),
            resp.getAggregations(),
            resp.getScrollId(),
            resp.getHits().getMaxScore()
        );
    }

    /** Total matching documents (not just page size). */
    public long total()           { return total; }

    /** Deserialized document objects for this page. */
    public List<T> hits()         { return hits; }

    /** ES _id for each hit (same order as hits()). */
    public List<String> ids()     { return ids; }

    /** Relevance score for each hit. */
    public List<Float> scores()   { return scores; }

    /** Raw ES Aggregations — use EsAgg to parse. */
    public Aggregations aggs()    { return aggregations; }

    /** Scroll ID for cursor-based pagination (null if scroll not requested). */
    public String scrollId()      { return scrollId; }

    public float maxScore()       { return maxScore; }

    public boolean isEmpty()      { return hits.isEmpty(); }

    public int size()             { return hits.size(); }

    @Override
    public String toString() {
        return String.format("EsResult[total=%d, hits=%d, maxScore=%.3f]", total, hits.size(), maxScore);
    }
}
