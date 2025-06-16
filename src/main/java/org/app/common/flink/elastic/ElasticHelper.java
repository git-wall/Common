package org.app.common.flink.elastic;

import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;
import java.util.function.Function;

public class ElasticHelper {
    /**
     * Creates an Elasticsearch sink with the provided hosts and sink function.
     *
     * @param hosts                   List of Elasticsearch HTTP hosts
     * @param elasticsearchSinkFunction Function to convert elements to Elasticsearch requests
     * @param <T>                     Type of elements to be indexed
     * @return Configured ElasticsearchSink
     */
    public static <T> ElasticsearchSink<T> createElasticsearchSink(
            List<HttpHost> hosts, 
            ElasticsearchEmitter<T> elasticsearchSinkFunction) {

        return new Elasticsearch7SinkBuilder<T>()
                .setHosts(hosts.toArray(new HttpHost[0]))
                .setEmitter(elasticsearchSinkFunction)
                .setBulkFlushMaxActions(500) // Flush after 500 actions
                .setBulkFlushInterval(3000) // Flush every 3 seconds
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 3, 1000) // Exponential backoff
                .build();
    }

    /**
     * Creates an Elasticsearch sink with the provided hosts and a function to convert elements to index requests.
     *
     * @param hosts       List of Elasticsearch HTTP hosts
     * @param indexName   Name of the Elasticsearch index
     * @param toJsonFunc  Function to convert elements to JSON string
     * @param <T>         Type of elements to be indexed
     * @return Configured ElasticsearchSink
     */
    public static <T> ElasticsearchSink<T> createElasticsearchSink(
            List<HttpHost> hosts, 
            String indexName, 
            Function<T, String> toJsonFunc) {

        return new Elasticsearch7SinkBuilder<T>()
                .setHosts(hosts.toArray(new HttpHost[0]))
                .setEmitter((element, context, indexer) -> {
                    IndexRequest request = new IndexRequest(indexName)
                            .source(toJsonFunc.apply(element), XContentType.JSON);
                    indexer.add(request);
                })
                .setBulkFlushInterval(3000) // Flush every 3 seconds
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 3, 1000) // Exponential backoff
                .build();
    }

    /**
     * Creates an Elasticsearch sink with the provided hosts and a function to convert elements to index requests with document IDs.
     *
     * @param hosts       List of Elasticsearch HTTP hosts
     * @param indexName   Name of the Elasticsearch index
     * @param idFunc      Function to extract document ID from elements
     * @param toJsonFunc  Function to convert elements to JSON string
     * @param <T>         Type of elements to be indexed
     * @return Configured ElasticsearchSink
     */
    public static <T> ElasticsearchSink<T> createElasticsearchSinkWithIds(
            List<HttpHost> hosts, 
            String indexName, 
            Function<T, String> idFunc,
            Function<T, String> toJsonFunc) {

        return new Elasticsearch7SinkBuilder<T>()
                .setHosts(hosts.toArray(new HttpHost[0]))
                .setEmitter((element, context, indexer) -> {
                    IndexRequest request = new IndexRequest(indexName)
                            .id(idFunc.apply(element))
                            .source(toJsonFunc.apply(element), XContentType.JSON);
                    indexer.add(request);
                })
                .setBulkFlushInterval(3000) // Flush every 3 seconds
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 3, 1000) // Exponential backoff
                .build();
    }

    /**
     * Creates an Elasticsearch sink with the provided hosts and a function to convert elements to index requests with document IDs and routing.
     *
     * @param hosts       List of Elasticsearch HTTP hosts
     * @param indexName   Name of the Elasticsearch index
     * @param idFunc      Function to extract document ID from elements
     * @param routingFunc Function to extract routing key from elements
     * @param toJsonFunc  Function to convert elements to JSON string
     * @param <T>         Type of elements to be indexed
     * @return Configured ElasticsearchSink
     */
    public static <T> ElasticsearchSink<T> createElasticsearchSinkWithRouting(
            List<HttpHost> hosts, 
            String indexName, 
            Function<T, String> idFunc,
            Function<T, String> routingFunc,
            Function<T, String> toJsonFunc) {

        return new Elasticsearch7SinkBuilder<T>()
                .setHosts(hosts.toArray(new HttpHost[0]))
                .setEmitter((element, context, indexer) -> {
                    IndexRequest request = new IndexRequest(indexName)
                            .id(idFunc.apply(element))
                            .routing(routingFunc.apply(element))
                            .source(toJsonFunc.apply(element), XContentType.JSON);
                    indexer.add(request);
                })
                .setBulkFlushInterval(3000) // Flush every 3 seconds
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 3, 1000) // Exponential backoff
                .build();
    }
}
