package org.app.common.job;

public interface JobContext {
    boolean isRunning();
    void stop();
}
