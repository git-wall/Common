//package org.app.common.flink.elastic;
//
//import com.google.gson.Gson;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.http.HttpHost;
//import org.app.common.flink.StreamEnvironment;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.common.xcontent.XContentType;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Example class demonstrating how to use ElasticHelper with Flink 2.0.0
// */
//public class ElasticHelperExample {
//
//    /**
//     * Example data class to be indexed in Elasticsearch
//     */
//    public static class Customer {
//        private String id;
//        private String name;
//        private String email;
//        private int age;
//
//        public Customer(String id, String name, String email, int age) {
//            this.id = id;
//            this.name = name;
//            this.email = email;
//            this.age = age;
//        }
//
//        public String getId() {
//            return id;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getEmail() {
//            return email;
//        }
//
//        public int getAge() {
//            return age;
//        }
//    }
//
//    /**
//     * Example of how to use ElasticHelper to create an Elasticsearch sink
//     */
//    public static void main(String[] args) throws Exception {
//        // Create Flink execution environment
//        StreamExecutionEnvironment env = StreamEnvironment.buildEnv("elastic-example", "1.0.0");
//
//        // Define Elasticsearch hosts
//        List<HttpHost> esHosts = Arrays.asList(
//                new HttpHost("localhost", 9200, "http"),
//                new HttpHost("localhost", 9201, "http")
//        );
//
//        // Create a sample data stream
//        DataStream<Customer> customerStream = env.fromElements(
//                new Customer("1", "John Doe", "john@example.com", 30),
//                new Customer("2", "Jane Smith", "jane@example.com", 25),
//                new Customer("3", "Bob Johnson", "bob@example.com", 40)
//        );
//
//        // Create a Gson instance for JSON serialization
//        Gson gson = new Gson();
//
//        // Example 1: Using the simple sink with JSON conversion function
//        customerStream.sinkTo(ElasticHelper.createElasticsearchSink(
//                esHosts,
//                "customers",
//                customer -> gson.toJson(customer)
//        ));
//
//        // Example 2: Using the sink with document IDs
//        customerStream.sinkTo(ElasticHelper.createElasticsearchSinkWithIds(
//                esHosts,
//                "customers",
//                Customer::getId,
//                customer -> gson.toJson(customer)
//        ));
//
//        // Example 3: Using the sink with custom emitter function
//        customerStream.sinkTo(ElasticHelper.createElasticsearchSink(
//                esHosts,
//                (element, context, indexer) -> {
//                    IndexRequest request = new IndexRequest("customers")
//                            .id(element.getId())
//                            .source(gson.toJson(element), XContentType.JSON);
//                    indexer.add(request);
//                }
//        ));
//
//        // Example 4: Using the sink with routing
//        customerStream.sinkTo(ElasticHelper.createElasticsearchSinkWithRouting(
//                esHosts,
//                "customers",
//                Customer::getId,
//                customer -> String.valueOf(customer.getAge() % 2), // Route by age parity
//                customer -> gson.toJson(customer)
//        ));
//
//        // Execute the Flink job
//        env.execute("Elasticsearch Example Job");
//    }
//}
