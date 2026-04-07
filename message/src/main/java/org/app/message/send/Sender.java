package org.app.message.send;

import org.app.observation.log.RequestLog;

import java.util.List;

public interface Sender extends AutoCloseable {

    void send(List<RequestLog> logs);
}
