package org.app.common.kafka.multi.processor;

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.kafka.multi.config.RetryConfig;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ParallelKafkaConsumer<K, V> {
    private final ParallelStreamProcessor<K, V> parallelConsumer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final MessageProcessor<K, V> processor;
    private final RetryConfig retryConfig;
    private final KafkaProducer<K, V> producer;

    public ParallelKafkaConsumer(KafkaConsumer<K, V> consumer,
                                 KafkaProducer<K, V> producer,
                                 MessageProcessor<K, V> processor,
                                 int maxConcurrency,
                                 RetryConfig retryConfig) {
        this.processor = processor;
        this.producer = producer;
        this.retryConfig = retryConfig;

        ParallelConsumerOptions<K, V> options = ParallelConsumerOptions.<K, V>builder()
                .consumer(consumer)
                .maxConcurrency(maxConcurrency)
                .ordering(ParallelConsumerOptions.ProcessingOrder.UNORDERED)
                .commitMode(ParallelConsumerOptions.CommitMode.PERIODIC_CONSUMER_ASYNCHRONOUS)
                .build();

        this.parallelConsumer = ParallelStreamProcessor.createEosStreamProcessor(options);
    }

    public void start(String topic) {
        try {
            parallelConsumer.subscribe(Collections.singletonList(topic));

            parallelConsumer.poll(context -> {
                ConsumerRecord<K, V> record = context.getSingleRecord().getConsumerRecord();
                processWithRetry(record, retryConfig.getMaxRetries());
            });
        } catch (Exception e) {
            log.error("Fatal error in consumer", e);
            shutdown();
            throw new RuntimeException("Consumer failed", e);
        }
    }

    private void processWithRetry(ConsumerRecord<K, V> record, int retryCount) {
        try {
            processor.process(record);
        } catch (Exception e) {
            retry(record, e, retryCount);
        }
    }

    private void retry(ConsumerRecord<K, V> record, Exception e, int retryCount) {
        switch (retryConfig.getStrategy()) {
            case IN_MEMORY_RETRY:
                inMemoryRetry(record, e, retryCount);
                break;
            case RETRY_TOPIC:
                sendToRetryTopic(record, retryCount);
                break;
            case DLQ_IMMEDIATE:
                sendToDlq(record, e);
                break;
            default:
                log.error("Unrecoverable error, no retry strategy defined", e);
                shutdown();
        }
    }

    private void inMemoryRetry(ConsumerRecord<K, V> record, Exception e, int retryCount) {
        if (retryCount < retryConfig.getMaxRetries()) {
            log.warn("Retry attempt {} of {}", retryCount + 1, retryConfig.getMaxRetries());
            try {
                Thread.sleep(retryConfig.getRetryDelay().toMillis());
                processWithRetry(record, retryCount + 1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                sendToDlq(record, e);
            }
        } else {
            sendToDlq(record, e);
        }
    }

    private void sendToRetryTopic(ConsumerRecord<K, V> record, int retryCount) {
        ProducerRecord<K, V> retryRecord = new ProducerRecord<>(
                retryConfig.getRetryTopic(),
                record.key(),
                record.value()
        );

        retryRecord.headers().add("retry_count", String.valueOf(retryCount).getBytes());

        producer.send(retryRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send to retry topic, sending to DLQ", exception);
                sendToDlq(record, exception);
            }
        });
    }

    private void sendToDlq(ConsumerRecord<K, V> record, Exception e) {
        ProducerRecord<K, V> dlqRecord = new ProducerRecord<>(
                retryConfig.getDlqTopic(),
                record.key(),
                record.value()
        );

        dlqRecord.headers().add("error_message", e.getMessage().getBytes());

        producer.send(dlqRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send to DLQ", exception);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            try {
                parallelConsumer.close();
                producer.close();
            } catch (Exception e) {
                log.error("Error during shutdown", e);
            }
        }
    }
}