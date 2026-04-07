package org.app.message.kafka.single.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

public abstract class KafkaTemplateConfig<K, V> {

    protected abstract KafkaProperties getKafkaProperties();
    protected abstract MeterRegistry getMeterRegistry();
    public abstract ConcurrentKafkaListenerContainerFactory<K, V> listenerContainerFactory();

    private static <T> JsonDeserializer<T> gettJsonDeserializer(Class<T> clazz) {
        var jsonDeserializer = new JsonDeserializer<>(clazz);
        jsonDeserializer.addTrustedPackages("*");
        return jsonDeserializer;
    }

    public JsonMessageConverter jsonMessageConverter() {
        return new ByteArrayJsonMessageConverter();
    }

    /**
     * <pre>{@code
     *         configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
     *         configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getKeySerializer());
     *         configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getValueSerializer());
     *         configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
     *         configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);
     *         configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
     *         configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
     *         configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProperties.getProducer().getCompressionType());
     *         configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
     *         configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
     *         configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
     *         configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
     * }</pre>
     * */
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(getKafkaProperties().buildProducerProperties());
    }

    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG
     * ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG
     * ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG
     * ConsumerConfig.RETRY_BACKOFF_MS_CONFIG
     * ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG
     * ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG
     * ConsumerConfig.FETCH_MIN_BYTES_CONFIG
     * ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG
     * */
    public ConsumerFactory<K, V> consumerFactory(Class<K> keyClass, Class<V> valueClass) {
        var kafkaProperties = getKafkaProperties();
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.putAll(kafkaProperties.getConsumer().getProperties());

        JsonDeserializer<K> keyDev = new JsonDeserializer<>(keyClass);
        JsonDeserializer<V> valDev = new JsonDeserializer<>(valueClass);

        // Protect message with (Poison Pill Pattern)
        ErrorHandlingDeserializer<K> errorKey = new ErrorHandlingDeserializer<>(keyDev);
        ErrorHandlingDeserializer<V> errorVal = new ErrorHandlingDeserializer<>(valDev);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<K, V> consumerFactory = new DefaultKafkaConsumerFactory<>(props, errorKey, errorVal);
        consumerFactory.addListener(new MicrometerConsumerListener<>(getMeterRegistry()));
        return consumerFactory;
    }

    public ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerManual(Class<K> keyClass, Class<V> valueClass) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(keyClass, valueClass));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    public ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerContainerFactory(Class<K> keyClass, Class<V> valueClass) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(keyClass, valueClass));
        return factory;
    }

    public RetryTopicConfiguration retryTopicConfiguration(KafkaTemplate<String, String> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .maxAttempts(3)
                .fixedBackOff(500)
                .includeTopic("${app.kafka.topic}")
                .retryTopicSuffix("-retry")
                .dltSuffix("-dlt")
                .autoCreateTopics(false, 3, (short) 1)
                .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
                .create(template);
    }
}
