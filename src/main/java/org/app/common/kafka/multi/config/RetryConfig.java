package org.app.common.kafka.multi.config;

import lombok.Builder;
import lombok.Getter;
import org.app.common.kafka.multi.processor.RetryStrategy;

import java.time.Duration;

@Builder
@Getter
public class RetryConfig {
    private final RetryStrategy strategy;
    private final int maxRetries;
    private final Duration retryDelay;
    private final String retryTopic;
    private final String dlqTopic;
}
