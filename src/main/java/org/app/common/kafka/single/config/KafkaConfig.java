package org.app.common.kafka.single.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.app.common.context.SpringContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.producer")
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getValueSerializer());
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProperties.getProducer().getCompressionType());
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.producer")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.consumer")
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, kafkaProperties.getConsumer().getFetchMinSize().toBytes());
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, kafkaProperties.getConsumer().getFetchMaxWait().toMillis());

        if (kafkaProperties.getConsumer().getProperties().containsKey("max.partition.fetch.bytes")) {
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("max.partition.fetch.bytes"));
        }

        if (kafkaProperties.getConsumer().getProperties().containsKey("max.poll.interval.ms")) {
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("max.poll.interval.ms"));
        }
        if (kafkaProperties.getConsumer().getProperties().containsKey("session.timeout.ms")) {
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("session.timeout.ms"));
        }
        if (kafkaProperties.getConsumer().getProperties().containsKey("retry.backoff.ms")) {
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("retry.backoff.ms"));
        }
        if (kafkaProperties.getConsumer().getProperties().containsKey("connections.max.idle.ms")) {
            props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("connections.max.idle.ms"));
        }
        if (kafkaProperties.getConsumer().getProperties().containsKey("request.timeout.ms")) {
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                    kafkaProperties.getConsumer().getProperties().get("request.timeout.ms"));
        }

        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        consumerFactory.addListener(
                new MicrometerConsumerListener<>(
                        SpringContext.getContext().getBean(MeterRegistry.class)
                ));
        return consumerFactory;
    }

    @Bean
    @ConditionalOnBean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

//    @Bean
//    @Deprecated
//    public RetryTopicConfiguration retryTopicConfiguration(KafkaTemplate<String, String> template) {
//        return RetryTopicConfigurationBuilder
//                .newInstance()
//                .maxAttempts(3)
//                .fixedBackOff(500)
//                .includeTopic("${app.kafka.topic}")
//                .retryTopicSuffix("-retry")
//                .dltSuffix("-dlt")
//                .autoCreateTopics(false, 3, (short) 1)
//                .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
//                .create(template);
//    }
}
