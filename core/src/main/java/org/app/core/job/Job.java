package org.app.core.job;

public interface Job {
    void init();

    void execute(JobContext context) throws Exception;

    void shutdown();
}

