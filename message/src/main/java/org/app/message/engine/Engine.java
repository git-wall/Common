package org.app.message.engine;

import org.app.observation.log.RequestLog;

public interface Engine extends AutoCloseable {

    void publish(RequestLog log);

    void shutdown();

    @Override
    default void close() {
        shutdown();
    }
}
