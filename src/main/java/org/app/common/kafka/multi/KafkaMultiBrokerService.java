package org.app.common.kafka.multi;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.app.common.kafka.multi.processor.MessageProcessor;
import org.app.common.kafka.multi.processor.ParallelKafkaConsumer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class KafkaMultiBrokerService {

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
        if (brokerManager.hasBroker(brokerId)) {
            brokerManager.startConsumer(brokerId, topic, messageProcessor, 10);
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
                                   MessageProcessor<String, String> messageProcessor) {

        if (brokerManager.hasBroker(brokerId)) {
            ParallelKafkaConsumer.RetryConfig retryConfig = ParallelKafkaConsumer.RetryConfig.builder()
                    .strategy(ParallelKafkaConsumer.RetryStrategy.IN_MEMORY_RETRY)
                    .maxRetries(0)
                    .retryDelay(Duration.ofSeconds(0L))
                    .dlqTopic(topic + "-dlq")
                    .build();

            brokerManager.startConsumer(brokerId, topic, messageProcessor, 10, retryConfig);
        }
    }

    /**
     * Retrieves a Kafka producer for the specified broker.
     *
     * @param brokerId the ID of the broker
     * @return the Kafka producer
     */
    public KafkaProducer<String, String> getProducer(String brokerId) {
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
        brokerManager.producerSend(brokerId, topic, key, value);
    }
}
