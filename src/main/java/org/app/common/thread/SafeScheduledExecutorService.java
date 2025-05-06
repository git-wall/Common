package org.app.common.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SafeScheduledExecutorService {
    private final Supplier<ScheduledExecutorService> executorSupplier;
    private volatile ScheduledExecutorService executor;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int maxFailuresBeforeReset;

    public SafeScheduledExecutorService(Supplier<ScheduledExecutorService> executorSupplier, int maxFailuresBeforeReset) {
        this.executorSupplier = executorSupplier;
        this.maxFailuresBeforeReset = maxFailuresBeforeReset;
        this.executor = executorSupplier.get();
    }

    public Future<?> submit(Runnable task) {
        ensureExecutorActive();
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            System.err.println("Executor was shutdown during submit, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.submit(task);
        }
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        ensureExecutorActive();
        try {
            return executor.schedule(task, delay, unit);
        } catch (RejectedExecutionException e) {
            System.err.println("Executor was shutdown during schedule, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.schedule(task, delay, unit);
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ensureExecutorActive();
        try {
            return executor.scheduleAtFixedRate(task, initialDelay, period, unit);
        } catch (RejectedExecutionException e) {
            System.err.println("Executor was shutdown during scheduleAtFixedRate, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.scheduleAtFixedRate(task, initialDelay, period, unit);
        }
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        ensureExecutorActive();
        try {
            return executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
        } catch (RejectedExecutionException e) {
            System.err.println("Executor was shutdown during scheduleWithFixedDelay, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
        }
    }

    private synchronized void ensureExecutorActive() {
        if (executor.isShutdown() || executor.isTerminated()) {
            System.err.println("Executor inactive, recreating...");
            resetExecutor();
        }
    }

    private synchronized void resetExecutor() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }
        } catch (Exception ignore) {
        }
        executor = executorSupplier.get();
        failureCount.set(0);
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void shutdownNow() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
