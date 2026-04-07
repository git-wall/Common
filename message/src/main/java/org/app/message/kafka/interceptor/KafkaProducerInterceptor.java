package org.app.message.kafka.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class KafkaProducerInterceptor implements ProducerInterceptor<String, String> {

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        var id = getId();

        producerRecord.headers().add("X-Request-Id", id.getBytes());
        return producerRecord;
    }

    private static String getId() {
        var id = MDC.get("X-Request-Id");

        if (id == null) {
            return UUID.randomUUID().toString();
        }
        return id;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
