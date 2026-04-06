package org.app.message.send;

import org.app.message.engine.Engine;
import org.app.observation.log.RequestLog;

public class Publisher {

    private final Engine engine;

    public Publisher(Engine engine) {
        this.engine = engine;
    }

    public void publish(RequestLog log) {
        engine.publish(log);
    }
}
