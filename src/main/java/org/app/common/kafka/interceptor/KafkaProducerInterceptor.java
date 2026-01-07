package org.app.common.kafka.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.app.common.context.TracingContext;
import org.app.common.utils.RequestUtils;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class KafkaProducerInterceptor implements ProducerInterceptor<String, String> {
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        String id = TracingContext.getRequestId();

        if (id == null) {
            id = MDC.get(RequestUtils.REQUEST_ID);
        }

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        producerRecord.headers().add(RequestUtils.REQUEST_ID, id.getBytes());
        return producerRecord;
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
