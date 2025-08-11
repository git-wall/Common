package org.app.common.spark;

import lombok.Getter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A utility class for processing data using Apache Spark with Elasticsearch, Kafka, and SQL.
 * This class provides methods for reading from and writing to various data sources.
 */
@Getter
public class SparkDataProcessor {

    private final JavaSparkContext sparkContext;

    private final SparkSession sparkSession;

    public SparkDataProcessor(String appName) {
        this.sparkContext = SparkContext.getSparkContext(appName);
        this.sparkSession = SparkSession.builder()
                .appName(appName)
                .config(sparkContext.getConf())
                .getOrCreate();
    }

    /**
     * Executes an SQL query on the given table.
     *
     * @param sqlQuery the SQL query to execute
     * @return the result of the query as a Dataset of Rows
     */
    public Dataset<Row> executeSql(String sqlQuery) {
        return sparkSession.sql(sqlQuery);
    }

    /**
     * Reads data from a JDBC source.
     *
     * @param url the JDBC URL
     * @param table the table name
     * @param properties the connection properties
     * @return the data as a Dataset of Rows
     */
    public Dataset<Row> readJdbc(String url, String table, Properties properties) {
        return sparkSession.read()
                .jdbc(url, table, properties);
    }

    /**
     * Writes data to a JDBC destination.
     *
     * @param dataset the data to write
     * @param url the JDBC URL
     * @param table the table name
     * @param properties the connection properties
     */
    public void writeJdbc(Dataset<Row> dataset, String url, String table, Properties properties) {
        dataset.write()
                .mode(SaveMode.Append)
                .jdbc(url, table, properties);
    }

    /**
     * Reads data from Elasticsearch.
     *
     * @param index the Elasticsearch index
     * @param options additional options for reading
     * @return the data as a Dataset of Rows
     */
    public Dataset<Row> readElasticsearch(String index, Map<String, String> options) {
        Map<String, String> configMap = new HashMap<>(options);
        configMap.put("es.resource", index);

        return sparkSession.read()
                .format("org.elasticsearch.spark.sql")
                .options(configMap)
                .load();
    }

    /**
     * Writes data to Elasticsearch.
     *
     * @param dataset the data to write
     * @param index the Elasticsearch index
     * @param options additional options for writing
     */
    public void writeElasticsearch(Dataset<Row> dataset, String index, Map<String, String> options) {
        Map<String, String> configMap = new HashMap<>(options);
        configMap.put("es.resource", index);

        dataset.write()
                .format("org.elasticsearch.spark.sql")
                .options(configMap)
                .mode(SaveMode.Append)
                .save();
    }

    /**
     * Creates a Kafka producer for sending messages.
     *
     * @param bootstrapServers the Kafka bootstrap servers
     * @param properties additional properties for the producer
     * @return a configured KafkaProducer
     */
    public KafkaProducer<String, String> createKafkaProducer(
            String bootstrapServers, 
            Properties properties) {

        Properties props = new Properties();
        props.putAll(properties);
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        return new KafkaProducer<>(props);
    }

    /**
     * Sends a message to a Kafka topic.
     *
     * @param producer the Kafka producer
     * @param topic the topic to send to
     * @param key the message key
     * @param value the message value
     */
    public void sendKafkaMessage(
            KafkaProducer<String, String> producer,
            String topic,
            String key,
            String value) {

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
    }

    /**
     * Reads data from Kafka as a batch.
     *
     * @param bootstrapServers the Kafka bootstrap servers
     * @param topics the topics to read from
     * @param options additional options for reading
     * @return the data as a Dataset of Rows
     */
    public Dataset<Row> readKafka(String bootstrapServers, Collection<String> topics, Map<String, String> options) {
        Map<String, String> kafkaOptions = new HashMap<>(options);
        kafkaOptions.put("kafka.bootstrap.servers", bootstrapServers);
        kafkaOptions.put("subscribe", String.join(",", topics));

        return sparkSession.read()
                .format("kafka")
                .options(kafkaOptions)
                .load();
    }

    /**
     * Writes data to Kafka.
     *
     * @param dataset the data to write
     * @param bootstrapServers the Kafka bootstrap servers
     * @param topic the topic to write to
     * @param options additional options for writing
     */
    public void writeKafka(Dataset<Row> dataset, String bootstrapServers, String topic, Map<String, String> options) {
        Map<String, String> kafkaOptions = new HashMap<>(options);
        kafkaOptions.put("kafka.bootstrap.servers", bootstrapServers);
        kafkaOptions.put("topic", topic);

        dataset.write()
                .format("kafka")
                .options(kafkaOptions)
                .save();
    }

    /**
     * Reads a CSV file.
     *
     * @param path the path to the CSV file
     * @param header whether the CSV file has a header
     * @param inferSchema whether to infer the schema
     * @return the data as a Dataset of Rows
     */
    public Dataset<Row> readCsv(String path, boolean header, boolean inferSchema) {
        return sparkSession.read()
                .option("header", header)
                .option("inferSchema", inferSchema)
                .csv(path);
    }

    /**
     * Reads a Parquet file.
     *
     * @param path the path to the Parquet file
     * @return the data as a Dataset of Rows
     */
    public Dataset<Row> readParquet(String path) {
        return sparkSession.read().parquet(path);
    }

    /**
     * Writes data to a Parquet file.
     *
     * @param dataset the data to write
     * @param path the path to write to
     */
    public void writeParquet(Dataset<Row> dataset, String path) {
        dataset.write().mode(SaveMode.Overwrite).parquet(path);
    }

    /**
     * Stops the Spark context and session.
     */
    public void stop() {
        if (sparkSession != null) {
            sparkSession.stop();
        }
        if (sparkContext != null) {
            sparkContext.stop();
        }
    }
}
