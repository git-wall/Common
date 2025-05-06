package org.app.common.thread;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
public class SafeExecutorService {
    private final Supplier<ExecutorService> executorSupplier;
    private volatile ExecutorService executor;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int maxFailuresBeforeReset;

    public SafeExecutorService() {
        this.executorSupplier = () -> Executors.newFixedThreadPool(5);
        this.maxFailuresBeforeReset = 3;
        this.executor = executorSupplier.get();
    }

    public SafeExecutorService(Supplier<ExecutorService> executorSupplier, int maxFailuresBeforeReset) {
        this.executorSupplier = executorSupplier;
        this.maxFailuresBeforeReset = maxFailuresBeforeReset;
        this.executor = executorSupplier.get();
    }

    public static SafeExecutorService simple() {
        return new SafeExecutorService();
    }

    public Future<?> submit(Runnable task) {
        ensureExecutorActive();
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            log.error("Executor was shutdown during submit, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.submit(task);
        }
    }

    public <T> Future<T> submit(Callable<T> task) {
        ensureExecutorActive();
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            log.error("Executor was shutdown during submit, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.submit(task);
        }
    }

    @SneakyThrows
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ensureExecutorActive();
        try {
            return executor.invokeAll(tasks);
        } catch (RejectedExecutionException e) {
            log.error("Executor was shutdown during invokeAll, recreating...");
            if (failureCount.incrementAndGet() > maxFailuresBeforeReset) {
                resetExecutor();
            } else {
                failureCount.set(failureCount.get() + 1);
            }
            return executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("invokeAll was interrupted", e);
            throw new RuntimeException("invokeAll was interrupted", e);
        }
    }

    private synchronized void ensureExecutorActive() {
        if (executor.isShutdown() || executor.isTerminated()) {
            log.info("Executor inactive, recreating...");
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