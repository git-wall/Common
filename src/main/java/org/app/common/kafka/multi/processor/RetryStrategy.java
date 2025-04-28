package org.app.common.kafka.multi.processor;

public enum RetryStrategy {
    // IN_MEMORY_RETRY: Retries processing in the same consumer
    IN_MEMORY_RETRY,
    // RETRY_TOPIC: Sends failed messages to a retry topic
    RETRY_TOPIC,
    // DLQ_IMMEDIATE: Sends failed messages directly to Dead Letter Queue
    DLQ_IMMEDIATE
}
