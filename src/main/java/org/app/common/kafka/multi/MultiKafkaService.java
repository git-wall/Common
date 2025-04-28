package org.app.common.kafka.multi;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.app.common.kafka.multi.config.RetryConfig;
import org.app.common.kafka.multi.processor.MessageProcessor;
import org.app.common.kafka.multi.processor.RetryStrategy;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MultiKafkaService {

    private final BrokerManager brokerManager;

    /**
     * Runs a Kafka consumer for the specified broker and topic with retry logic.
     *
     * <p>
     * Retry topic: retryTopic(topic + "-retry") <br>
     * Dead letter queue: dlqTopic(topic + "-dlq")
     * </p>
     *
     * @param brokerId         the ID of the broker
     * @param topic            the topic to consume messages from
     * @param messageProcessor the processor to handle consumed messages
     */
    public void runConsumer(String brokerId, String topic, MessageProcessor<String, String> messageProcessor) {
        if (isAlreadyBroker(brokerId)) {
            brokerManager.startConsumer(brokerId, topic, messageProcessor, ParallelConsumerOptions.DEFAULT_MAX_CONCURRENCY);
        }
    }

    /**
     * Runs a Kafka consumer for the specified broker and topic with retry logic.
     *
     * <p>
     * This method starts a Kafka consumer for the given broker and topic, using the provided
     * message processor to handle consumed messages. It also applies the specified retry
     * configuration to handle message processing failures.
     * </p>
     *
     * @param brokerId         the ID of the broker
     * @param topic            the topic to consume messages from
     * @param messageProcessor the processor to handle consumed messages
     * @param retryConfig      the configuration for retry logic, including retry strategy,
     *                         maximum retries, and delay between retries
     */
    public void runConsumer(String brokerId,
                            String topic,
                            MessageProcessor<String, String> messageProcessor,
                            RetryConfig retryConfig,
                            int maxConcurrency) {

        if (isAlreadyBroker(brokerId)) {
            brokerManager.startConsumer(brokerId, topic, messageProcessor, maxConcurrency, retryConfig);
        }
    }

    /**
     * Runs a Kafka consumer for the specified broker and topic without retry logic.
     *
     * @param brokerId         the ID of the broker
     * @param topic            the topic to consume messages from
     * @param messageProcessor the processor to handle consumed messages
     */
    public void runConsumerNoRetry(String brokerId,
                                   String topic,
                                   MessageProcessor<String, String> messageProcessor,
                                   int maxConcurrency) {

        if (isAlreadyBroker(brokerId)) {
            RetryConfig retryConfig = RetryConfig.builder()
                    .strategy(RetryStrategy.IN_MEMORY_RETRY)
                    .maxRetries(0)
                    .retryDelay(Duration.ofSeconds(0L))
                    .dlqTopic(topic + "-dlq")
                    .build();

            brokerManager.startConsumer(brokerId, topic, messageProcessor, maxConcurrency, retryConfig);
        }
    }

    /**
     * Retrieves a Kafka producer for the specified broker.
     *
     * @param brokerId the ID of the broker
     * @return the Kafka producer
     */
    public KafkaProducer<String, String> getProducer(String brokerId) {
        if (!isAlreadyBroker(brokerId)) {
            throw new IllegalArgumentException("Broker not found: " + brokerId);
        }
        return brokerManager.getProducer(brokerId);
    }

    /**
     * Sends a message using the Kafka producer for the specified broker and topic.
     *
     * @param brokerId the ID of the broker
     * @param topic    the topic to send the message to
     * @param key      the key of the message
     * @param value    the value of the message
     */
    public void producerSend(String brokerId, String topic, String key, Object value) {
        if (!isAlreadyBroker(brokerId)) {
            throw new IllegalArgumentException("Broker not found: " + brokerId);
        }
        brokerManager.producerSend(brokerId, topic, key, value);
    }

    private boolean isAlreadyBroker(String brokerId) {
        return brokerManager.hasBroker(brokerId);
    }
}
