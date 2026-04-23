package org.app.security.shield;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.jackson.JacksonUtils;
import org.app.security.shield.event.AuthSecurityEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthEventSender {

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public AuthEventSender(
        KafkaProducer<String, String> producer,
        @Value("${monitor.log.kafka.topic}") String topic) {
        this.topic = topic;
        this.producer = producer;
    }

    public void send(AuthSecurityEvent event) {
        String key = event.subject != null ? event.subject : event.ip;
        String payload = JacksonUtils.writeValueAsString(event);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

        record
            .headers()
            .add("eventType", event.eventType.name().getBytes());

        producer.send(record, (meta, ex) -> {
            if (ex != null) {
                log.error("Kafka send failed", ex);
            }
        });
    }
}
