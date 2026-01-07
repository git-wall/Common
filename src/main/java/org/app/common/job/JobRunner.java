package org.app.common.job;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

@Slf4j
public class JobRunner implements AutoCloseable {
    private final ExecutorService executor;
    private final List<JobHandle> jobs = new CopyOnWriteArrayList<>();

    public JobRunner(ExecutorService executor) {
        this.executor = executor;
    }

    public void submit(Job job) {
        JobHandle handle = new JobHandle(job);
        jobs.add(handle);
        executor.submit(() -> run(handle));
    }

    private void run(JobHandle handle) {
        Job job = handle.job;
        JobContext ctx = handle.context;
        job.init();
        try {
            while (ctx.isRunning()) {
                job.execute(ctx);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Job error", e);
        } finally {
            job.shutdown();
        }
    }

    @Override
    public void close() {
        jobs.forEach(JobHandle::stop);
        executor.shutdown();
    }
}
