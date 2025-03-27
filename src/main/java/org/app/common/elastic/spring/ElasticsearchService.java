package org.app.common.elastic.spring;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ElasticsearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Saves a document to the specified index.
     *
     * @param indexName The name of the index
     * @param document The document to save
     * @param <T> The type of the document
     * @return The saved document
     */
    public <T> T save(String indexName, T document) {
        return elasticsearchOperations.save(document, IndexCoordinates.of(indexName));
    }

    /**
     * Finds a document by its ID.
     *
     * @param indexName The name of the index
     * @param id The document ID
     * @param clazz The class type of the document
     * @param <T> The type of the document
     * @return The found document or null if not found
     */
    public <T> T findById(String indexName, String id, Class<T> clazz) {
        return elasticsearchOperations.get(id, clazz, IndexCoordinates.of(indexName));
    }

    /**
     * Deletes a document by its ID.
     *
     * @param indexName The name of the index
     * @param id The document ID
     * @param clazz The class type of the document
     * @return The deleted document's ID
     */
    public String deleteById(String indexName, String id, Class<?> clazz) {
        return elasticsearchOperations.delete(id, IndexCoordinates.of(indexName));
    }

    /**
     * Searches for documents using a query string.
     *
     * @param indexName The name of the index
     * @param queryString The query string
     * @param clazz The class type of the documents
     * @param <T> The type of the documents
     * @return A list of matching documents
     */
    public <T> List<T> searchByQueryString(String indexName, String queryString, Class<T> clazz) {
        QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryString);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        
        SearchHits<T> searchHits = elasticsearchOperations.search(
                searchQuery, clazz, IndexCoordinates.of(indexName));
        
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Searches for documents with pagination and sorting.
     *
     * @param indexName The name of the index
     * @param field The field to search in
     * @param value The value to search for
     * @param page The page number (0-based)
     * @param size The page size
     * @param sortField The field to sort by
     * @param ascending Whether to sort in ascending order
     * @param clazz The class type of the documents
     * @param <T> The type of the documents
     * @return A page of matching documents
     */
    public <T> Page<T> searchWithPagination(
            String indexName,
            String field,
            String value,
            int page,
            int size,
            String sortField,
            boolean ascending,
            Class<T> clazz) {
        
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Criteria criteria = new Criteria(field).is(value);
        Query searchQuery = new CriteriaQuery(criteria)
                .setPageable(pageRequest);
        
        SearchHits<T> searchHits = elasticsearchOperations.search(
                searchQuery, clazz, IndexCoordinates.of(indexName));
        
        List<T> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
                content, pageRequest, searchHits.getTotalHits());
    }

    /**
     * Performs a more like this query to find similar documents.
     *
     * @param indexName The name of the index
     * @param id The ID of the document to find similar documents to
     * @param fields The fields to consider for similarity
     * @param clazz The class type of the documents
     * @param <T> The type of the documents
     * @return A list of similar documents
     */
    public <T> List<T> findSimilar(String indexName, String id, String[] fields, Class<T> clazz) {
        MoreLikeThisQuery query = new MoreLikeThisQuery();
        query.setId(id);
        query.addFields(fields);
        
        SearchHits<T> searchHits = elasticsearchOperations.search(
                query, clazz, IndexCoordinates.of(indexName));
        
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Counts the number of documents matching a criteria.
     *
     * @param indexName The name of the index
     * @param field The field to match
     * @param value The value to match
     * @param clazz The class type of the documents
     * @return The count of matching documents
     */
    public long count(String indexName, String field, String value, Class<?> clazz) {
        Criteria criteria = new Criteria(field).is(value);
        Query searchQuery = new CriteriaQuery(criteria);
        
        return elasticsearchOperations.count(searchQuery, clazz, IndexCoordinates.of(indexName));
    }
}