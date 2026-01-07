package org.app.common.job;

import java.util.concurrent.atomic.AtomicBoolean;

class DefaultJobContext implements JobContext {
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void stop() {
        running.set(false);
    }
}
