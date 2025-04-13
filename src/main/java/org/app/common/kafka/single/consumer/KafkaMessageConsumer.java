package org.app.common.kafka.single.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.kafka.single.handler.MessageHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final MessageHandler<String> messageHandler;

    /**
     * <p>
     * <h4>What it looks like in action:</h4>
     * Message → my-topic: <br>
     * Fails → my-topic-retry-0 (after 1s) <br>
     * Fails → my-topic-retry-1 (after 2s) <br>
     * Fails → my-topic-retry-2 (after 4s) <br>
     * Fails → my-topic-dlt: Processed by messageHandler.dlt
     * </p>
     */
    @RetryableTopic(
            attempts = "${spring.kafka.retry.max-attempts}",
            backoff = @Backoff(
                    delay = 1000L,    // first retry 1s
                    multiplier = 2.0, // next retry will be * 2
                    maxDelay = 10000L // but capped at 10s
            ),
            autoCreateTopics = "true",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
    )
    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processMessage(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//                             @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) Optional<String> dltExceptionMessage,
//                             @Header(KafkaHeaders.DLT_EXCEPTION_STACKTRACE) Optional<String> dltExceptionStacktrace,
                               Acknowledgment ack) {
        try {
            if (topic.endsWith("-dlt")) {
                messageHandler.dlt(message, topic);
            } else if (topic.contains("-retry")) {
                messageHandler.retry(message, topic);
            } else {
                messageHandler.handle(message, topic);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }
}