package org.app.core.job;

public interface JobContext {
    boolean isRunning();
    void stop();
}
