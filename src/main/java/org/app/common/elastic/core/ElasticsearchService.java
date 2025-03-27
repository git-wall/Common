package org.app.common.elastic.core;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticsearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Creates an index in Elasticsearch.
     *
     * @param indexName The name of the index to create.
     * @return True if the index was created successfully, false otherwise.
     */
    public boolean createIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest.Builder().index(indexName).build();
        return elasticsearchClient.indices().create(request).acknowledged();
    }

    /**
     * Checks if an index exists in Elasticsearch.
     *
     * @param indexName The name of the index to check.
     * @return True if the index exists, false otherwise.
     */
    public boolean indexExists(String indexName) throws IOException {
        ExistsRequest request = new ExistsRequest.Builder().index(indexName).build();
        BooleanResponse response = elasticsearchClient.indices().exists(request);
        return response.value();
    }

    /**
     * Deletes an index from Elasticsearch.
     *
     * @param indexName The name of the index to delete.
     * @return True if the index was deleted successfully, false otherwise.
     */
    public boolean deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest.Builder().index(indexName).build();
        return elasticsearchClient.indices().delete(request).acknowledged();
    }

    /**
     * Indexes a document in Elasticsearch.
     *
     * @param indexName The name of the index.
     * @param id        The ID of the document.
     * @param document  The document to index.
     */
    public void indexDocument(String indexName, String id, Object document) throws IOException {
        IndexRequest<Object> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(id)
                .document(document)
        );
        elasticsearchClient.index(request);
    }

    /**
     * Searches for documents in Elasticsearch.
     *
     * @param indexName The name of the index.
     * @param fn        A function to build the query.
     * @return A list of matching documents.
     */
    public <T> Collection<T> searchDocuments(String indexName,
                                             Class<T> clazz,
                                             Function<Query.Builder, ObjectBuilder<Query>> fn) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(fn)
        );
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        HitsMetadata<T> hits = response.hits();
        return hits.hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Updates a document in Elasticsearch.
     *
     * @param indexName The name of the index.
     * @param id        The ID of the document.
     * @param document  The updated document.
     */
    public void updateDocument(String indexName, String id, Object document) throws IOException {
        UpdateRequest<Object, Object> request = UpdateRequest.of(u -> u
                .index(indexName)
                .id(id)
                .doc(document)
        );
        elasticsearchClient.update(request, Object.class);
    }

    /**
     * Deletes a document from Elasticsearch.
     *
     * @param indexName The name of the index.
     * @param id        The ID of the document to delete.
     */
    public void deleteDocument(String indexName, String id) throws IOException {
        DeleteRequest request = DeleteRequest.of(d -> d
                .index(indexName)
                .id(id)
        );
        elasticsearchClient.delete(request);
    }

    /**
     * Performs a paginated search with sorting options.
     *
     * @param indexName The name of the index.
     * @param clazz     The class type of the documents.
     * @param queryFn   A function to build the query.
     * @param from      The starting index for pagination.
     * @param size      The number of results to return.
     * @param sortFn    A function to build the sort options.
     * @return A SearchResult containing the documents and metadata.
     */
    public <T> SearchResult<T> searchWithPagination(
            String indexName,
            Class<T> clazz,
            Function<Query.Builder, ObjectBuilder<Query>> queryFn,
            int from,
            int size,
            Function<SortOptions.Builder, ObjectBuilder<SortOptions>> sortFn) throws IOException {
        
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(queryFn)
                .from(from)
                .size(size)
                .sort(sortFn)
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        List<T> documents = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
        
        TotalHits totalHits = response.hits().total();
        long total = totalHits != null ? totalHits.value() : 0L;
        
        return new SearchResult<>(documents, total, from, size);
    }

    /**
     * Performs a search with aggregations.
     *
     * @param indexName    The name of the index.
     * @param clazz        The class type of the documents.
     * @param queryFn      A function to build the query.
     * @param aggregations A map of aggregation name to aggregation builder.
     * @return A map of aggregation name to aggregation response.
     */
    public <T> Map<String, Aggregate> searchWithAggregations(
            String indexName,
            Class<T> clazz,
            Function<Query.Builder, ObjectBuilder<Query>> queryFn,
            Map<String, Function<Aggregation.Builder, ObjectBuilder<Aggregation>>> aggregations) throws IOException {
        
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(indexName)
                .query(queryFn)
                .size(0); // We don't need documents, just aggregations
        
        // Add all aggregations to the request
        aggregations.forEach(requestBuilder::aggregations
        );
        
        SearchResponse<T> response = elasticsearchClient.search(requestBuilder.build(), clazz);
        return response.aggregations();
    }

    /**
     * Performs a multi-match query across multiple fields.
     *
     * @param indexName The name of the index.
     * @param clazz     The class type of the documents.
     * @param text      The text to search for.
     * @param fields    The fields to search in.
     * @return A list of matching documents.
     */
    public <T> Collection<T> multiMatchQuery(
            String indexName,
            Class<T> clazz,
            String text,
            String... fields) throws IOException {
        
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .multiMatch(mm -> mm
                                .query(text)
                                .fields(List.of(fields))
                        )
                )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Performs a term query for exact matching.
     *
     * @param indexName The name of the index.
     * @param clazz     The class type of the documents.
     * @param field     The field to search in.
     * @param value     The exact value to match.
     * @return A list of matching documents.
     */
    public <T> Collection<T> termQuery(
            String indexName,
            Class<T> clazz,
            String field,
            Object value) throws IOException {
        
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .term(t -> t
                                .field(field)
                                .value(v -> v.stringValue(value.toString()))
                        )
                )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Performs a range query for numeric or date fields.
     *
     * @param indexName The name of the index.
     * @param clazz     The class type of the documents.
     * @param field     The field to search in.
     * @param from      The lower bound (inclusive).
     * @param to        The upper bound (inclusive).
     * @return A list of matching documents.
     */
    public <T> Collection<T> rangeQuery(
            String indexName,
            Class<T> clazz,
            String field,
            Object from,
            Object to) throws IOException {
        
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .range(r -> r
                                .field(field)
                                .gte(from != null ? JsonData.fromJson(from.toString()) : null)
                                .lte(to != null ? JsonData.fromJson(to.toString()) : null)
                        )
                )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Performs a fuzzy search for approximate matching.
     *
     * @param indexName  The name of the index.
     * @param clazz      The class type of the documents.
     * @param field      The field to search in.
     * @param value      The value to match approximately.
     * @param fuzziness  The fuzziness parameter (e.g., "AUTO", "1", "2").
     * @return A list of matching documents.
     */
    public <T> Collection<T> fuzzyQuery(
            String indexName,
            Class<T> clazz,
            String field,
            String value,
            String fuzziness) throws IOException {
        
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .fuzzy(f -> f
                                .field(field)
                                .value(value)
                                .fuzziness(fuzziness)
                        )
                )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**
     * Result class for paginated searches.
     *
     * @param <T> The type of documents in the search result.
     */
    @Getter
    public static class SearchResult<T> {
        private final List<T> documents;
        private final long totalHits;
        private final int from;
        private final int size;

        public SearchResult(List<T> documents, long totalHits, int from, int size) {
            this.documents = documents;
            this.totalHits = totalHits;
            this.from = from;
            this.size = size;
        }

        public int getTotalPages() {
            return size > 0 ? (int) Math.ceil((double) totalHits / (double) size) : 0;
        }

        public boolean hasNext() {
            return (long) (from + size) < totalHits;
        }

        public boolean hasPrevious() {
            return from > 0;
        }
    }
}