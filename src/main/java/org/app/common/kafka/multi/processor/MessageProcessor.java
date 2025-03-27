package org.app.common.kafka.multi.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@FunctionalInterface
public interface MessageProcessor<K, V> {
    void process(ConsumerRecord<K, V> record) throws Exception;
}