package org.app.common.elastic.spring;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.util.ObjectBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ElasticQuery {
    private final NativeQueryBuilder queryBuilder = NativeQuery.builder();
    private final List<Consumer<BoolQuery.Builder>> queryConsumers = new ArrayList<>(8);
    private final List<Consumer<BoolQuery.Builder>> filterConsumers = new ArrayList<>(8);

    /**
     * Add a term query (exact match)
     */
    public ElasticQuery term(String field, Object value) {
        if (value != null) {
            filterConsumers.add(b -> b.must(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value.toString())))));
        }
        return this;
    }

    /**
     * Add a match query (analyzed text)
     */
    public ElasticQuery match(String field, String value) {
        if (StringUtils.hasText(value)) {
            queryConsumers.add(b -> b.must(q -> q.match(m -> m.field(field).query(value))));
        }
        return this;
    }

    /**
     * Add a multi-match query across multiple fields
     */
    public ElasticQuery multiMatch(String value, String... fields) {
        if (StringUtils.hasText(value) && fields.length > 0) {
            queryConsumers.add(b -> b.must(q -> q.multiMatch(m -> m.fields(List.of(fields)).query(value))));
        }
        return this;
    }

    /**
     * Add a fuzzy multi-match query across multiple fields
     */
    public ElasticQuery fuzzyMultiMatch(String value, String fuzziness, String... fields) {
        if (StringUtils.hasText(value) && fields.length > 0) {
            queryConsumers.add(b -> b.must(q -> q.multiMatch(m -> m
                    .fields(List.of(fields))
                    .query(value)
                    .fuzziness(fuzziness))));
        }
        return this;
    }

    /**
     * Add a range query
     */
    public ElasticQuery range(String field, Object from, Object to) {
        if (from != null || to != null) {
            filterConsumers.add(b -> b.must(q -> q.range(r -> {
                RangeQuery.Builder builder = r.field(field);
                if (from != null) builder.from(from.toString());
                if (to != null) builder.to(to.toString());
                return builder;
            })));
        }
        return this;
    }

    /**
     * Add a should match query (OR condition)
     */
    public ElasticQuery should(String field, String value) {
        if (StringUtils.hasText(value)) {
            queryConsumers.add(b -> b.should(q -> q.match(m -> m.field(field).query(value))));
        }
        return this;
    }

    /**
     * Add a must not query (NOT condition)
     */
    public ElasticQuery mustNot(String field, Object value) {
        if (value != null) {
            filterConsumers.add(b -> b.mustNot(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value.toString())))));
        }
        return this;
    }

    /**
     * Add multiple term filters for a field with comma-separated values
     */
    public ElasticQuery termsFilter(String field, String commaSeparatedValues) {
        if (StringUtils.hasText(commaSeparatedValues)) {
            String[] values = commaSeparatedValues.split(",");
            filterConsumers.add(b -> {
                BoolQuery.Builder innerBool = new BoolQuery.Builder();
                for (String value : values) {
                    innerBool.should(q -> q.term(t -> t.field(field).value(v -> v.stringValue(value.trim()))));
                }
                b.must(q -> q.bool(innerBool.build()));
            });
        }
        return this;
    }

    /**
     * Add pagination
     */
    public ElasticQuery page(Pageable pageable) {
        if (pageable != null) {
            queryBuilder.withPageable(pageable);
        }
        return this;
    }

    /**
     * Add source filtering (include/exclude fields)
     */
    public ElasticQuery sourceFilter(String[] includes, String[] excludes) {
        queryBuilder.withSourceFilter(new org.springframework.data.elasticsearch.core.query.FetchSourceFilter(includes, excludes));
        return this;
    }

    /**
     * Add an aggregation
     */
    public ElasticQuery aggregation(String name, Function<co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder,
            ObjectBuilder<co.elastic.clients.elasticsearch._types.aggregations.Aggregation>> fn) {
        queryBuilder.withAggregation(name, fn.apply(new co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder()).build());
        return this;
    }

    /**
     * Add a customizer for the query builder
     */
    public ElasticQuery customize(Consumer<NativeQueryBuilder> customizer) {
        customizer.accept(queryBuilder);
        return this;
    }

    /**
     * Build the final query
     */
    public NativeQuery build() {
        // Apply query clauses if any
        if (!queryConsumers.isEmpty()) {
            queryBuilder.withQuery(q -> {
                BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
                queryConsumers.forEach(consumer -> consumer.accept(boolBuilder));
                return q.bool(boolBuilder.build());
            });
        }

        // Apply filter clauses if any
        if (!filterConsumers.isEmpty()) {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            filterConsumers.forEach(consumer -> consumer.accept(boolBuilder));
            queryBuilder.withFilter(boolBuilder.build()._toQuery());
        }
        return queryBuilder.build();
    }
}