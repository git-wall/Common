package org.app.common.kafka.multi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.app.common.kafka.multi.config.KafkaMultiBrokerProperties;
import org.app.common.kafka.multi.config.KafkaMultiBrokerProperties.BrokerConfig;
import org.app.common.kafka.multi.processor.MessageProcessor;
import org.app.common.kafka.multi.processor.ParallelKafkaConsumer;
import org.app.common.kafka.multi.processor.ParallelKafkaConsumer.RetryConfig;
import org.app.common.kafka.multi.processor.ParallelKafkaConsumer.RetryStrategy;
import org.app.common.thread.ThreadUtils;
import org.app.common.utils.JacksonUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaMultiBrokerProperties.class)
public class BrokerManager {
    private final Map<String, KafkaProducer<String, String>> producers = new ConcurrentHashMap<>(4);
    private final Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>(4);
    private final KafkaMultiBrokerProperties brokerProperties;

    @PostConstruct
    public void init() {
        brokerProperties.getConfigs().forEach(this::initializeBroker);
        ThreadUtils.RuntimeBuilder.shutDownHook(this::shutdown);
    }

    private void initializeBroker(String brokerId, BrokerConfig config) {
        switch (config.getType()) {
            case PRODUCER_ONLY:
                initializeProducer(brokerId, config);
                break;
            case CONSUMER_ONLY:
                initializeConsumer(brokerId, config);
                break;
            case BOTH:
                initializeProducer(brokerId, config);
                initializeConsumer(brokerId, config);
                break;
        }
    }

    private void initializeProducer(String brokerId, BrokerConfig config) {
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

    private void initializeConsumer(String brokerId, BrokerConfig config) {
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

    public boolean hasBroker(String brokerId) {
        return brokerProperties.getConfigs().containsKey(brokerId);
    }

    public KafkaProducer<String, String> getProducer(String brokerId) {
        return producers.get(brokerId);
    }

    public void producerSend(String brokerId, String topic, String key, Object value) {
        var val = JacksonUtils.toString(value);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, val);
        producers.get(brokerId).send(record);
    }

    public void startConsumer(
            String brokerId,
            String topic,
            MessageProcessor<String, String> messageProcessor,
            int concurrency) {

        KafkaConsumer<String, String> consumer = consumers.get(brokerId);
        KafkaProducer<String, String> producer = producers.get(brokerId);

        RetryConfig retryConfig = RetryConfig.builder()
                .strategy(RetryStrategy.RETRY_TOPIC)
                .maxRetries(3)
                .retryDelay(Duration.ofSeconds(3L))
                .retryTopic(topic + "-retry")
                .dlqTopic(topic + "-dlq")
                .build();

        ParallelKafkaConsumer<String, String> parallelConsumer = new ParallelKafkaConsumer<>(
                consumer,
                producer,
                messageProcessor,
                concurrency,
                retryConfig
        );

        Thread consumerThread = new Thread(() -> parallelConsumer.start(topic));
        consumerThread.setName(String.format("parallel-consumer-%s-%s", brokerId, topic));
        consumerThread.start();

        log.info("Started parallel consumer for broker: {} on topic: {} with retry configuration", brokerId, topic);
    }

    // Add overloaded method for custom retry configuration
    public void startConsumer(String brokerId,
                              String topic,
                              MessageProcessor<String, String> messageProcessor,
                              int concurrency,
                              RetryConfig retryConfig) {

        KafkaConsumer<String, String> consumer = consumers.get(brokerId);
        KafkaProducer<String, String> producer = producers.get(brokerId);

        ParallelKafkaConsumer<String, String> parallelConsumer = new ParallelKafkaConsumer<>(
                consumer,
                producer,
                messageProcessor,
                concurrency,
                retryConfig
        );

        Thread consumerThread = new Thread(() -> parallelConsumer.start(topic));
        consumerThread.setName(String.format("parallel-consumer-%s-%s", brokerId, topic));
        consumerThread.start();

        log.info("Started parallel consumer for broker: {} on topic: {} with custom retry configuration", brokerId, topic);
    }

    public void shutdown() {
        consumers.values().forEach(KafkaConsumer::close);
        producers.values().forEach(KafkaProducer::close);
        consumers.clear();
        producers.clear();
        log.info("All broker manager shutdown completed");
    }
}