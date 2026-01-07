package org.app.common.kafka.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.RequestUtils;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KafkaRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> consumerRecord) {
        return null;
    }

    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        Header header = record.headers().lastHeader(RequestUtils.REQUEST_ID);
        MDC.put(RequestUtils.REQUEST_ID, new String(header.value(), StandardCharsets.UTF_8));
        var value = JacksonUtils.toJson(record.value());

        log.info(
            "Kafka record: topic {}, partition {}, offset {}, key {}, value {}, header {}",
            record.topic(), record.partition(), record.offset(), record.key(), record.value(), value
        );

        return record;
    }

    @Override
    public void success(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        RecordInterceptor.super.success(record, consumer);
    }

    @Override
    public void failure(ConsumerRecord<K, V> record, Exception exception, Consumer<K, V> consumer) {
        RecordInterceptor.super.failure(record, exception, consumer);
    }

    @Override
    public void afterRecord(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        RecordInterceptor.super.afterRecord(record, consumer);
    }
}
