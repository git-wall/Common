package org.app.common.db.map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.kafka.multi.BrokerManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Pipeline {
    private final MapDbProperties properties;
    private final KafkaProducer<String, String> producers;
    private final MemoryCache memoryCache;

    public Pipeline(MapDbProperties properties, BrokerManager brokerManager, MemoryCache memoryCache) {
        this.properties = properties;
        this.memoryCache = memoryCache;
        this.producers = brokerManager.getProducer(properties.getKafka().getBrokerId());
    }

    public void createTable(String table) {
        if (properties.getCache().isEnabled()) {
            memoryCache.createCache(table);
        }
        if (properties.getKafka().isEnabled()) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    properties.getKafka().getTopic(),
                    "CreateTable",
                    table);
            producers.send(record);
        }
        if (properties.getLog().isEnabled()) {
            log.info("Action Create Table: {}", table);
        }
    }

    public void put(String table, String key, String value) {
        if (properties.getCache().isEnabled()) {
            memoryCache.put(table, key, value);
        }

        if (properties.getKafka().isEnabled()) {
            send("Put", table, key, value);
        }

        if (properties.getLog().isEnabled()) {
            log(table, key, value);
        }
    }

    public void delete(String table, String key) {
        if (properties.getCache().isEnabled()) {
            memoryCache.remove(table, key);
        }

        if (properties.getKafka().isEnabled()) {
            send("Delete", table, key, "");
        }

        if (properties.getLog().isEnabled()) {
            log(table, key, "");
        }
    }

    private void send(String action, String table, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                properties.getKafka().getTopic(), action,
                String.format("Table %s Key %s Val %s", table, key, value));
        producers.send(record);
    }

    private static void log(String table, String key, String value) {
        log.info("Table {} Key {} Val {}", table, key, value);
    }
}
