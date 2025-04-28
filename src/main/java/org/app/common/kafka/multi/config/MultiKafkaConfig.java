package org.app.common.kafka.multi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.app.common.kafka.multi.BrokerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MultiKafkaBrokerProperties.class)
public class MultiKafkaConfig {

    private final MultiKafkaBrokerProperties brokerProperties;

    @Bean
    @ConditionalOnMissingBean
    public BrokerManager brokerManager() {
        Map<String, KafkaProducer<String, String>> producers = new ConcurrentHashMap<>();
        Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>();
        
        brokerProperties.getConfigs().forEach((brokerId, config) -> 
            initializeBroker(brokerId, config, producers, consumers));
            
        return new BrokerManager(producers, consumers, brokerProperties);
    }
    
    private void initializeBroker(String brokerId, 
                                 MultiKafkaBrokerProperties.BrokerConfig config,
                                 Map<String, KafkaProducer<String, String>> producers,
                                 Map<String, KafkaConsumer<String, String>> consumers) {
        switch (config.getType()) {
            case PRODUCER_ONLY:
                initializeProducer(brokerId, config, producers);
                break;
            case CONSUMER_ONLY:
                initializeConsumer(brokerId, config, consumers);
                break;
            case BOTH:
                initializeProducer(brokerId, config, producers);
                initializeConsumer(brokerId, config, consumers);
                break;
        }
    }

    private void initializeProducer(String brokerId, 
                                   MultiKafkaBrokerProperties.BrokerConfig config,
                                   Map<String, KafkaProducer<String, String>> producers) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        if (config.getProducer() != null) {
            props.setProperty(ProducerConfig.ACKS_CONFIG, config.getProducer().getAcks());
            props.put(ProducerConfig.RETRIES_CONFIG, config.getProducer().getRetries());
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getProducer().getBatchSize());
            props.put(ProducerConfig.LINGER_MS_CONFIG, config.getProducer().getLingerMs());
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getProducer().getBufferMemory());
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.getProducer().getCompressionType());
        }

        producers.put(brokerId, new KafkaProducer<>(props));
        log.info("Initialized producer for broker: {}", brokerId);
    }

    private void initializeConsumer(String brokerId, 
                                   MultiKafkaBrokerProperties.BrokerConfig config,
                                   Map<String, KafkaConsumer<String, String>> consumers) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        if (config.getConsumer() != null) {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getConsumer().isEnableAutoCommit());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getConsumer().getAutoOffsetReset());
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getConsumer().getMaxPollRecords());
        }

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumers.put(brokerId, consumer);
        log.info("Initialized consumer for broker: {}", brokerId);
    }
}
