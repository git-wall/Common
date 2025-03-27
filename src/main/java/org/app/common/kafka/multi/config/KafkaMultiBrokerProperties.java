package org.app.common.kafka.multi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "kafka.brokers")
public class KafkaMultiBrokerProperties {
    private Map<String, BrokerConfig> configs = new HashMap<>();

    @Data
    public static class BrokerConfig {
        private String bootstrapServers;
        private String groupId;
        private BrokerType type;
        private ProducerConfig producer;
        private ConsumerConfig consumer;
    }

    @Data
    public static class ProducerConfig {
        private String acks = "all";
        private int retries = 3;
        private int batchSize = 16384;
        private int lingerMs = 1;
        private int bufferMemory = 33554432;
        private String compressionType = "snappy";
    }

    @Data
    public static class ConsumerConfig {
        private boolean enableAutoCommit = false;
        private String autoOffsetReset = "earliest";
        private int maxPollRecords = 500;
    }

    public enum BrokerType {
        PRODUCER_ONLY,
        CONSUMER_ONLY,
        BOTH
    }
}