package org.app.database.elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregation result parser — wraps raw ES {@link Aggregations} into clean Java types.
 * <p>
 * Always obtained from an {@link EsResult} that was searched with aggregations.
 *
 * <pre>
 * EsResult&lt;?&gt; result = EsSearch.on(ops)
 *     .index("orders")
 *     .aggTerms("by_status", "status", 10)
 *     .aggSum("revenue", "amount")
 *     .aggAvg("avg_amount", "amount")
 *     .aggDateHistogram("by_day", "created_at", "day")
 *     .size(0).searchRaw();
 *
 * EsAgg agg = EsAgg.from(result);
 *
 * // Terms buckets: {PAID=1200, PENDING=300, CANCELLED=50}
 * Map&lt;String,Long&gt; byStatus = agg.terms("by_status");
 *
 * // Metrics
 * double revenue  = agg.sum("revenue");
 * double avgAmt   = agg.avg("avg_amount");
 * double minAmt   = agg.min("min_amount");
 * double maxAmt   = agg.max("max_amount");
 * long   distinct = agg.cardinality("unique_users");
 *
 * // Date histogram buckets: {"2024-01-01"=230, "2024-01-02"=415, ...}
 * Map&lt;String,Long&gt; byDay = agg.dateHistogram("by_day");
 * </pre>
 */
public final class EsAgg {

    private final Aggregations aggs;

    private EsAgg(Aggregations aggs) { this.aggs = aggs; }

    public static EsAgg from(EsResult<?> result) {
        if (result.aggs() == null) throw new IllegalStateException("No aggregations in result");
        return new EsAgg(result.aggs());
    }

    public static EsAgg from(Aggregations aggs) { return new EsAgg(aggs); }

    // -------------------------------------------------------------------------
    // Terms
    // -------------------------------------------------------------------------

    /**
     * Return terms buckets as ordered map: {bucketKey → docCount}.
     * Preserves ES bucket order (typically by docCount desc).
     */
    public Map<String, Long> terms(String aggName) {
        Terms terms = aggs.get(aggName);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Terms.Bucket b : terms.getBuckets()) {
            result.put(b.getKeyAsString(), b.getDocCount());
        }
        return result;
    }

    /**
     * Return terms buckets as list of {@link Bucket} (key + count + sub-aggs).
     */
    public List<Bucket> termsBuckets(String aggName) {
        Terms terms = aggs.get(aggName);
        return terms.getBuckets().stream()
            .map(b -> new Bucket(b.getKeyAsString(), b.getDocCount(), b.getAggregations()))
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Date Histogram
    // -------------------------------------------------------------------------

    /**
     * Return date histogram buckets as ordered map: {dateString → docCount}.
     */
    public Map<String, Long> dateHistogram(String aggName) {
        Histogram hist = aggs.get(aggName);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Histogram.Bucket b : hist.getBuckets()) {
            result.put(b.getKeyAsString(), b.getDocCount());
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Metric aggregations
    // -------------------------------------------------------------------------

    public double sum(String aggName) {
        Sum sum = aggs.get(aggName);
        return sum.getValue();
    }

    public double avg(String aggName) {
        Avg avg = aggs.get(aggName);
        return avg.getValue();
    }

    public double min(String aggName) {
        Min min = aggs.get(aggName);
        return min.getValue();
    }

    public double max(String aggName) {
        Max max = aggs.get(aggName);
        return max.getValue();
    }

    public long valueCount(String aggName) {
        ValueCount vc = aggs.get(aggName);
        return vc.getValue();
    }

    public long cardinality(String aggName) {
        Cardinality c = aggs.get(aggName);
        return c.getValue();
    }

    // -------------------------------------------------------------------------
    // Raw access
    // -------------------------------------------------------------------------

    public <A extends org.elasticsearch.search.aggregations.Aggregation> A raw(String aggName) {
        return aggs.get(aggName);
    }

    // -------------------------------------------------------------------------
    // Bucket record
    // -------------------------------------------------------------------------
    @Data
    @AllArgsConstructor
    public static class Bucket {
        String key;
        long docCount;
        Aggregations subAggs;
        public EsAgg subAgg() { return EsAgg.from(subAggs); }
    }
}
