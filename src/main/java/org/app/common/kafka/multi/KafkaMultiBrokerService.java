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
     * <p>
     * Retry topic: retryTopic(topic + "-retry") <br>
     * Dead letter queue: dlqTopic(topic + "-dlq")
     * </p>
     */
    public void runConsumer(String brokerId, String topic, MessageProcessor<String, String> messageProcessor) {
        if (brokerManager.hasBroker(brokerId)) {
            brokerManager.startConsumer(brokerId, topic, messageProcessor, 10);
        }
    }

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

    public KafkaProducer<String, String> getProducer(String brokerId) {
        return brokerManager.getProducer(brokerId);
    }

    public void producerSend(String brokerId, String topic, String key, Object value) {
        brokerManager.producerSend(brokerId, topic, key, value);
    }
}
