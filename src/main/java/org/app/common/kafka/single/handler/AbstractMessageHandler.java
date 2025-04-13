package org.app.common.kafka.single.handler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

    @Override
    public void handle(T message, String topic) throws Exception {
        try {
            doHandle(message);
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, message, e);
            throw e;
        }
    }

    @Override
    public void retry(T message, String topic) throws Exception {
        try {
            doRetry(message);
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, message, e);
            throw e;
        }
    }

    @Override
    public void dlt(T message, String topic) throws Exception {
        try {
            doDlt(message);
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, message, e);
            throw e;
        }
    }

    protected abstract void doHandle(T message) throws Exception;

    protected abstract void doRetry(T message) throws Exception;

    protected abstract void doDlt(T message) throws Exception;
}