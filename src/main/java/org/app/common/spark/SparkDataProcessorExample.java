package org.app.common.spark;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Example usage of the SparkDataProcessor class.
 */
public class SparkDataProcessorExample {
    public void test() {
        // Create a SparkDataProcessor instance
        SparkDataProcessor processor = new SparkDataProcessor("SparkDataProcessorExample");

        try {
            // Example 1: Execute SQL query
            System.out.println("Example 1: Execute SQL query");
            // First, create a temporary view from a data source
            processor.readCsv("path/to/data.csv", true, true)
                    .createOrReplaceTempView("data_table");

            // Then execute a SQL query on the view
            Dataset<Row> sqlResult = processor.executeSql("SELECT * FROM data_table WHERE value > 100");
            sqlResult.show();

            // Example 2: Read from and write to Elasticsearch
            System.out.println("Example 2: Read from and write to Elasticsearch");
            Map<String, String> esOptions = new HashMap<>();
            esOptions.put("es.nodes", "localhost");
            esOptions.put("es.port", "9200");

            // Read from Elasticsearch
            Dataset<Row> esData = processor.readElasticsearch("my_index", esOptions);
            esData.show();

            // Write to Elasticsearch
            processor.writeElasticsearch(esData, "my_output_index", esOptions);

            // Example 3: Read from and write to Kafka
            System.out.println("Example 3: Read from and write to Kafka");
            Map<String, String> kafkaOptions = new HashMap<>();
            kafkaOptions.put("startingOffsets", "earliest");

            // Read from Kafka
            Dataset<Row> kafkaData = processor.readKafka(
                    "localhost:9092",
                    Arrays.asList("input-topic"),
                    kafkaOptions);
            kafkaData.show();

            // Write to Kafka
            processor.writeKafka(
                    kafkaData,
                    "localhost:9092",
                    "output-topic",
                    new HashMap<>());

            // Example 4: Send individual messages to Kafka
            System.out.println("Example 4: Send individual messages to Kafka");
            Properties kafkaProps = new Properties();
            KafkaProducer<String, String> producer = processor.createKafkaProducer(
                    "localhost:9092",
                    kafkaProps);

            processor.sendKafkaMessage(producer, "my-topic", "key1", "value1");
            processor.sendKafkaMessage(producer, "my-topic", "key2", "value2");

            producer.close();

            // Example 5: Read from and write to JDBC
            System.out.println("Example 5: Read from and write to JDBC");
            Properties jdbcProps = new Properties();
            jdbcProps.put("user", "username");
            jdbcProps.put("password", "password");

            // Read from JDBC
            Dataset<Row> jdbcData = processor.readJdbc(
                    "jdbc:postgresql://localhost:5432/mydb",
                    "users",
                    jdbcProps);
            jdbcData.show();

            // Write to JDBC
            processor.writeJdbc(
                    jdbcData,
                    "jdbc:postgresql://localhost:5432/mydb",
                    "users_backup",
                    jdbcProps);

            // Example 6: Read and write Parquet files
            System.out.println("Example 6: Read and write Parquet files");
            Dataset<Row> parquetData = processor.readParquet("path/to/data.parquet");
            parquetData.show();

            processor.writeParquet(parquetData, "path/to/output.parquet");

        } finally {
            // Always stop the Spark context when done
            processor.stop();
        }
    }
}
