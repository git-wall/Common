package org.app.common.kafka.single.handler;

public interface MessageHandler<T> {
    void handle(T message, String topic) throws Exception;

    void retry(T message, String topic) throws Exception;

    void dlt(T message, String topic) throws Exception;
}