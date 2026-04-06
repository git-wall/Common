package org.app.message.send;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.app.jackson.JacksonUtils;
import org.app.observation.log.RequestLog;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>{@code
 * Properties props = new Properties();
 * props.put("bootstrap.servers", "localhost:9092");
 * props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
 * props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
 * props.put("acks", "1");
 * props.put("linger.ms", 20);
 * props.put("batch.size", 32768);
 * props.put("compression.type", "lz4");
 * props.put("buffer.memory", 67108864);
 * max.block.ms
 * | config        | tác dụng      |
 * | ------------- | ------------- |
 * | linger.ms     | Kafka batch   |
 * | batch.size    | batch bigger  |
 * | compression   | low network   |
 * | buffer.memory | non block     |
 * }</pre>
 */
@Slf4j
public class KafkaSender implements Sender {

    private final KafkaProducer<String, String> producer;

    private final String topic;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public KafkaSender(KafkaProducer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void send(List<RequestLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        for (RequestLog logEvent : logs) {
            try {
                String key = buildKey(logEvent);
                String payload = JacksonUtils.writeValueAsString(logEvent);
                ProducerRecord<String, String> r = new ProducerRecord<>(topic, key, payload);

                producer.send(r, (RecordMetadata metadata, Exception exception) -> {
                    if (exception != null) {
                        log.error(
                            "Kafka send failed topic={} partition={} offset={} error={}",
                            topic,
                            metadata != null ? metadata.partition() : null,
                            metadata != null ? metadata.offset() : null,
                            exception.getMessage(),
                            exception
                        );
                    }
                });
            } catch (Exception e) {
                log.error("Serialize log failed", e);
            }
        }
    }

    private String buildKey(RequestLog log) {
        if (log.getTraceId() != null) {
            return log.getTraceId();
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                producer.flush();
            } catch (Exception e) {
                log.warn("Kafka flush failed", e);
            }

            try {
                producer.close();
            } catch (Exception e) {
                log.warn("Kafka producer close failed", e);
            }
        }
    }

}
