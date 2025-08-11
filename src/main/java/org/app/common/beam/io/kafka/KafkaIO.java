package org.app.common.beam.io.kafka;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.app.common.beam.io.AbstractBeamIO;
import org.app.common.beam.io.IOOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of BeamIO for Kafka operations.
 * This class provides functionality to read from and write to Kafka topics using Apache Beam.
 *
 * @param <K> The type of Kafka record keys
 * @param <V> The type of Kafka record values
 */
public class KafkaIO<K, V> extends AbstractBeamIO<V> {
    
    private static final String BOOTSTRAP_SERVERS = "bootstrapServers";
    private static final String TOPIC = "topic";
    private static final String CONSUMER_CONFIG = "consumerConfig";
    private static final String PRODUCER_CONFIG = "producerConfig";
    private static final String KEY_DESERIALIZER = "keyDeserializer";
    private static final String VALUE_DESERIALIZER = "valueDeserializer";
    private static final String KEY_SERIALIZER = "keySerializer";
    private static final String VALUE_SERIALIZER = "valueSerializer";
    private static final String KEY_FUNCTION = "keyFunction";
    
    /**
     * Constructor with options.
     *
     * @param options Configuration options for the Kafka IO operation
     */
    public KafkaIO(IOOptions options) {
        super(options);
    }
    
    /**
     * Create a new KafkaIO instance with the specified configuration.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic
     * @param keyDeserializer  Class name of the key deserializer
     * @param valueDeserializer Class name of the value deserializer
     * @param <K>              The type of Kafka record keys
     * @param <V>              The type of Kafka record values
     * @return A new KafkaIO instance
     */
    public static <K, V> KafkaIO<K, V> read(
            String bootstrapServers, 
            String topic,
            String keyDeserializer,
            String valueDeserializer) {
        
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put("bootstrap.servers", bootstrapServers);
        consumerConfig.put("group.id", "beam-" + topic);
        consumerConfig.put("auto.offset.reset", "earliest");
        
        IOOptions options = new IOOptions()
                .set(BOOTSTRAP_SERVERS, bootstrapServers)
                .set(TOPIC, topic)
                .set(CONSUMER_CONFIG, consumerConfig)
                .set(KEY_DESERIALIZER, keyDeserializer)
                .set(VALUE_DESERIALIZER, valueDeserializer);
        
        return new KafkaIO<>(options);
    }
    
    /**
     * Create a new KafkaIO instance for writing to Kafka.
     *
     * @param bootstrapServers Kafka bootstrap servers
     * @param topic            Kafka topic
     * @param keySerializer    Class name of the key serializer
     * @param valueSerializer  Class name of the value serializer
     * @param keyFunction      Function to extract the key from the value
     * @param <K>              The type of Kafka record keys
     * @param <V>              The type of Kafka record values
     * @return A new KafkaIO instance
     */
    public static <K, V> KafkaIO<K, V> write(
            String bootstrapServers, 
            String topic,
            String keySerializer,
            String valueSerializer,
            Function<V, K> keyFunction) {
        
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put("bootstrap.servers", bootstrapServers);
        
        IOOptions options = new IOOptions()
                .set(BOOTSTRAP_SERVERS, bootstrapServers)
                .set(TOPIC, topic)
                .set(PRODUCER_CONFIG, producerConfig)
                .set(KEY_SERIALIZER, keySerializer)
                .set(VALUE_SERIALIZER, valueSerializer)
                .set(KEY_FUNCTION, keyFunction);
        
        return new KafkaIO<>(options);
    }
    
    @Override
    public PCollection<V> read(Pipeline pipeline) {
        if (!validateOptions()) {
            throw new IllegalStateException("Invalid options for KafkaIO");
        }
        
        String bootstrapServers = options.get(BOOTSTRAP_SERVERS);
        String topic = options.get(TOPIC);
        Map<String, Object> consumerConfig = options.get(CONSUMER_CONFIG);
        String keyDeserializer = options.get(KEY_DESERIALIZER);
        String valueDeserializer = options.get(VALUE_DESERIALIZER);
        
        return pipeline
                .apply("ReadFromKafka", org.apache.beam.sdk.io.kafka.KafkaIO.<K, V>read()
                        .withBootstrapServers(bootstrapServers)
                        .withTopic(topic)
                        .withKeyDeserializer(getDeserializerKeyClass(keyDeserializer))
                        .withValueDeserializer(getDeserializerValueClass(valueDeserializer))
                        .withConsumerConfigUpdates(consumerConfig)
                        .withoutMetadata())
                .apply("ExtractValues", Values.create());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void write(PCollection<V> input) {
        if (!validateOptions()) {
            throw new IllegalStateException("Invalid options for KafkaIO");
        }
        
        String bootstrapServers = options.get(BOOTSTRAP_SERVERS);
        String topic = options.get(TOPIC);
        Map<String, Object> producerConfig = options.get(PRODUCER_CONFIG);
        String keySerializer = options.get(KEY_SERIALIZER);
        String valueSerializer = options.get(VALUE_SERIALIZER);
        Function<V, K> keyFunction = options.get(KEY_FUNCTION);

        PCollection<KV<K, V>> kvPCollection = (PCollection<KV<K, V>>) (
                PCollection<?>) input
                .apply("CreateKV", MapElements
                        .into(TypeDescriptors.kvs(TypeDescriptor.of(Object.class), TypeDescriptor.of(Object.class)))
                        .via((V value) -> KV.of(keyFunction.apply(value), value))
                );

        kvPCollection.apply("WriteToKafka", org.apache.beam.sdk.io.kafka.KafkaIO.<K, V>write()
                .withBootstrapServers(bootstrapServers)
                .withTopic(topic)
                .withKeySerializer(getSerializerKeyClass(keySerializer))
                .withValueSerializer(getSerializerValueClass(valueSerializer))
                .withProducerConfigUpdates(producerConfig));
    }
    
    @Override
    protected boolean validateOptions() {
        boolean baseValidation = super.validateOptions() && 
                options.has(BOOTSTRAP_SERVERS) && 
                options.has(TOPIC);
                
        // For read operations
        boolean readValidation = options.has(CONSUMER_CONFIG) && 
                options.has(KEY_DESERIALIZER) && 
                options.has(VALUE_DESERIALIZER);
                
        // For write operations
        boolean writeValidation = options.has(PRODUCER_CONFIG) && 
                options.has(KEY_SERIALIZER) && 
                options.has(VALUE_SERIALIZER) && 
                options.has(KEY_FUNCTION);
                
        return baseValidation && (readValidation || writeValidation);
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends Deserializer<K>> getDeserializerKeyClass(String className) {
        try {
            return (Class<? extends Deserializer<K>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Deserializer class not found: " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Deserializer<V>> getDeserializerValueClass(String className) {
        try {
            return (Class<? extends Deserializer<V>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Deserializer class not found: " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Serializer<K>> getSerializerKeyClass(String className) {
        try {
            return (Class<? extends Serializer<K>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Serializer class not found: " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Serializer<V>> getSerializerValueClass(String className) {
        try {
            return (Class<? extends Serializer<V>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Serializer class not found: " + className, e);
        }
    }
}