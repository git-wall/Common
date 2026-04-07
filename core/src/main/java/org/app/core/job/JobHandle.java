package org.app.core.job;

public class JobHandle {
    final Job job;
    final JobContext context = new DefaultJobContext();

    JobHandle(Job job) {
        this.job = job;
    }

    void stop() {
        context.stop();
    }
}
