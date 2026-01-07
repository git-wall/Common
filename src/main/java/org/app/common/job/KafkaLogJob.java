package org.app.common.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.app.common.entities.log.RequestLog;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.TokenUtils;

import java.util.concurrent.BlockingQueue;

@Slf4j
@AllArgsConstructor
public class KafkaLogJob implements Job {
    private final BlockingQueue<RequestLog> queue;
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final String application;

    @Override
    public void init() {
    }

    @Override
    public void execute(JobContext ctx) throws Exception {
        try {
            RequestLog entity = queue.take();
            if (entity == RequestLog.EMPTY) {
                ctx.stop();
                return;
            }
            send(entity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error sending log", e);
        }
    }

    private void send(RequestLog entity) {
        try {
            var token = TokenUtils.generateId("log_user", 16);
            var key = application + ":" + token;

            producer.send(
                new ProducerRecord<>(topic, key, JacksonUtils.toJson(entity))
            );
        } catch (Exception e) {
            log.error("Error sending log", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            producer.flush();
        } catch (Exception e) {
            log.warn("Flush failed", e);
        }
    }
}

