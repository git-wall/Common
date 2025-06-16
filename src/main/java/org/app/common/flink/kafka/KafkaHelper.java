package org.app.common.flink.kafka;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Helper class for creating Kafka sources and sinks in Flink 2.0.0.
 * Provides methods with various levels of configuration options.
 */
public class KafkaHelper {

    /**
     * Builds a basic Kafka source with minimal configuration.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic to consume from
     * @param groupId          Consumer group ID
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSource(String bootstrapServers, String topic, String groupId) {
        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();
    }

    /**
     * Builds a Kafka source with custom starting offsets.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic to consume from
     * @param groupId          Consumer group ID
     * @param offsetsInitializer Starting offsets initializer
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSource(
            String bootstrapServers, 
            String topic, 
            String groupId,
            OffsetsInitializer offsetsInitializer) {
        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(offsetsInitializer)
                .build();
    }

    /**
     * Builds a Kafka source with multiple topics.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topics           List of Kafka topics to consume from
     * @param groupId          Consumer group ID
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSource(
            String bootstrapServers, 
            List<String> topics, 
            String groupId) {
        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topics)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();
    }

    /**
     * Builds a fully customizable Kafka source.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topics           List of Kafka topics to consume from
     * @param groupId          Consumer group ID
     * @param offsetsInitializer Starting offsets initializer
     * @param deserializationSchema Deserialization schema for Kafka records
     * @param properties       Additional Kafka consumer properties
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSource(
            String bootstrapServers,
            List<String> topics,
            String groupId,
            OffsetsInitializer offsetsInitializer,
            KafkaRecordDeserializationSchema<T> deserializationSchema,
            Properties properties) {

        KafkaSourceBuilder<T> builder = KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topics)
                .setGroupId(groupId)
                .setStartingOffsets(offsetsInitializer);

        if (deserializationSchema != null) {
            builder.setDeserializer(deserializationSchema);
        }

        if (properties != null) {
            builder.setProperties(properties);
        }

        return builder.build();
    }

    /**
     * Builds a Kafka source with a custom deserialization schema.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic to consume from
     * @param groupId          Consumer group ID
     * @param deserializationSchema Deserialization schema for values
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSource(
            String bootstrapServers,
            String topic,
            String groupId,
            DeserializationSchema<T> deserializationSchema) {

        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(deserializationSchema)
                .build();
    }

    /**
     * Builds a Kafka source with pattern-based topic subscription.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topicPattern     Pattern to match topic names
     * @param groupId          Consumer group ID
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSourceWithPattern(
            String bootstrapServers,
            Pattern topicPattern,
            String groupId) {

        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopicPattern(topicPattern)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();
    }

    /**
     * Builds a Kafka source with specific partition offsets.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic to consume from
     * @param groupId          Consumer group ID
     * @param offsetsInitializer Starting offsets initializer
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSourceWithOffsets(
            String bootstrapServers,
            String topic,
            String groupId,
            OffsetsInitializer offsetsInitializer) {

        return KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(offsetsInitializer)
                .build();
    }

    /**
     * Builds a Kafka source with custom consumer properties.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic to consume from
     * @param groupId          Consumer group ID
     * @param properties       Additional Kafka consumer properties
     * @param <T>              Type of elements to be consumed
     * @return Configured KafkaSource
     */
    public static <T> KafkaSource<T> buildKafkaSourceWithProperties(
            String bootstrapServers,
            String topic,
            String groupId,
            Properties properties) {

        KafkaSourceBuilder<T> builder = KafkaSource.<T>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest());

        if (properties != null) {
            builder.setProperties(properties);
        }

        return builder.build();
    }

    /**
     * Builds a basic Kafka sink with key and value serializers.
     *
     * @param keySer    Serialization schema for keys
     * @param valueSer  Serialization schema for values
     * @param brokers   Kafka bootstrap servers
     * @param topic     Kafka topic to produce to
     * @param <T>       Type of elements to be produced
     * @return Configured KafkaSink
     */
    public static <T> KafkaSink<T> buildKafkaSink(
            SerializationSchema<T> keySer,
            SerializationSchema<T> valueSer,
            String brokers,
            String topic) {

        return KafkaSink.<T>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(valueSer)
                        .setKeySerializationSchema(keySer)
                        .build()
                )
                .build();
    }

    /**
     * Builds a basic Kafka sink with only value serializer.
     *
     * @param valueSer  Serialization schema for values
     * @param brokers   Kafka bootstrap servers
     * @param topic     Kafka topic to produce to
     * @param <T>       Type of elements to be produced
     * @return Configured KafkaSink
     */
    public static <T> KafkaSink<T> buildKafkaSink(
            SerializationSchema<T> valueSer, 
            String brokers, 
            String topic) {

        return KafkaSink.<T>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(valueSer)
                        .build()
                )
                .build();
    }

    /**
     * Builds a Kafka sink with custom producer properties.
     *
     * @param valueSer   Serialization schema for values
     * @param brokers    Kafka bootstrap servers
     * @param topic      Kafka topic to produce to
     * @param properties Additional Kafka producer properties
     * @param <T>        Type of elements to be produced
     * @return Configured KafkaSink
     */
    public static <T> KafkaSink<T> buildKafkaSink(
            SerializationSchema<T> valueSer,
            String brokers,
            String topic,
            Properties properties) {

        KafkaSinkBuilder<T> builder = KafkaSink.<T>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(valueSer)
                        .build()
                );

        if (properties != null) {
            builder.setKafkaProducerConfig(properties);
        }

        return builder.build();
    }

    /**
     * Builds a Kafka sink with key serializer and custom producer properties.
     *
     * @param keySer     Serialization schema for keys
     * @param valueSer   Serialization schema for values
     * @param brokers    Kafka bootstrap servers
     * @param topic      Kafka topic to produce to
     * @param properties Additional Kafka producer properties
     * @param <T>        Type of elements to be produced
     * @return Configured KafkaSink
     */
    public static <T> KafkaSink<T> buildKafkaSink(
            SerializationSchema<T> keySer,
            SerializationSchema<T> valueSer,
            String brokers,
            String topic,
            Properties properties) {

        KafkaSinkBuilder<T> builder = KafkaSink.<T>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(valueSer)
                        .setKeySerializationSchema(keySer)
                        .build()
                );

        if (properties != null) {
            builder.setKafkaProducerConfig(properties);
        }

        return builder.build();
    }

    /**
     * Builds a Kafka sink with a transactional ID prefix.
     *
     * @param valueSer             Serialization schema for values
     * @param brokers              Kafka bootstrap servers
     * @param topic                Kafka topic to produce to
     * @param transactionalIdPrefix Transactional ID prefix
     * @param <T>                  Type of elements to be produced
     * @return Configured KafkaSink
     */
    public static <T> KafkaSink<T> buildKafkaSinkWithTransactions(
            SerializationSchema<T> valueSer,
            String brokers,
            String topic,
            String transactionalIdPrefix) {

        return KafkaSink.<T>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(valueSer)
                        .build()
                )
                .setTransactionalIdPrefix(transactionalIdPrefix)
                .build();
    }
}
