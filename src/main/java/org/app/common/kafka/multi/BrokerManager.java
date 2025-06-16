package org.app.common.kafka.multi;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.kafka.multi.config.MultiKafkaBrokerProperties;
import org.app.common.kafka.multi.config.RetryConfig;
import org.app.common.kafka.multi.processor.MessageProcessor;
import org.app.common.kafka.multi.processor.ParallelKafkaConsumer;
import org.app.common.kafka.multi.processor.RetryStrategy;
import org.app.common.thread.ThreadUtils;
import org.app.common.utils.JacksonUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class BrokerManager {
    private final Map<String, KafkaProducer<String, String>> producers;
    private final Map<String, KafkaConsumer<String, String>> consumers;
    private final MultiKafkaBrokerProperties brokerProperties;

    public BrokerManager(
            Map<String, KafkaProducer<String, String>> producers,
            Map<String, KafkaConsumer<String, String>> consumers,
            MultiKafkaBrokerProperties brokerProperties) {
        this.producers = producers;
        this.consumers = consumers;
        this.brokerProperties = brokerProperties;
    }

    @PostConstruct
    public void init() {
        ThreadUtils.RuntimeBuilder.addShutdownHook(this::shutdown);
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

    @PreDestroy
    public void shutdown() {
        consumers.values().forEach(KafkaConsumer::close);
        producers.values().forEach(KafkaProducer::close);
        consumers.clear();
        producers.clear();
        log.info("All broker manager shutdown completed");
    }
}