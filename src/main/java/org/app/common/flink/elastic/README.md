# Elasticsearch Helper for Flink 2.0.0

This package provides utility classes for integrating Apache Flink 2.0.0 with Elasticsearch 7.x.

## ElasticHelper

The `ElasticHelper` class provides several methods to create Elasticsearch sinks for Flink DataStreams:

1. **Basic Sink with Custom Emitter**
   ```java
   ElasticsearchSink<T> createElasticsearchSink(
       List<HttpHost> hosts, 
       ElasticsearchEmitter<T> elasticsearchSinkFunction)
   ```

2. **Sink with JSON Conversion**
   ```java
   ElasticsearchSink<T> createElasticsearchSink(
       List<HttpHost> hosts, 
       String indexName, 
       Function<T, String> toJsonFunc)
   ```

3. **Sink with Document IDs**
   ```java
   ElasticsearchSink<T> createElasticsearchSinkWithIds(
       List<HttpHost> hosts, 
       String indexName, 
       Function<T, String> idFunc,
       Function<T, String> toJsonFunc)
   ```

4. **Sink with Routing**
   ```java
   ElasticsearchSink<T> createElasticsearchSinkWithRouting(
       List<HttpHost> hosts, 
       String indexName, 
       Function<T, String> idFunc,
       Function<T, String> routingFunc,
       Function<T, String> toJsonFunc)
   ```

## Usage Example

See `ElasticHelperExample.java` for a complete example of how to use these methods.

Basic usage:

```java
// Define Elasticsearch hosts
List<HttpHost> esHosts = Arrays.asList(
        new HttpHost("localhost", 9200, "http")
);

// Create a Gson instance for JSON serialization
Gson gson = new Gson();

// Create an Elasticsearch sink
ElasticsearchSink<Customer> esSink = ElasticHelper.createElasticsearchSink(
        esHosts,
        "customers",
        customer -> gson.toJson(customer)
);

// Add the sink to your DataStream
customerStream.sinkTo(esSink);
```

## Configuration

All sinks are configured with:
- Bulk flush interval: 3000ms (3 seconds)
- Exponential backoff strategy with 3 retries and 1000ms initial delay

## Dependencies

This implementation requires:
- Apache Flink 2.0.0
- Flink Elasticsearch Connector 4.0.0-2.0
- Elasticsearch 7.x client libraries