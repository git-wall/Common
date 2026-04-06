package org.app.concurrent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * <pre>
 * Threads ≈ CPU Cores × (1 + Wait Time IO/Handling Time)
 * If you have 4 CPU cores:
 * - the average wait time for IO is two times the handling time
 * - the wait time for IO usually is above 2 the handling time
 * - 4 x (1 + 2) = 12 threads
 * - you can have around 12 threads running concurrently.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtils {

    public static final int CORE_AVAILABLE;

    static {
        CORE_AVAILABLE = Runtime.getRuntime().availableProcessors();
    }

    public static int getActiveCount() {
        // Returns the number of active threads in the current thread's thread group
        return Thread.activeCount();
    }

    public static int getCoreAvailable() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void addShutdownHook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }

    public static ExecutorService fixed(String name, int size, int queueSize) {
        return new ThreadPoolExecutor(
            size,
            size,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(queueSize),
            new NamedThreadFactory(name),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public static ExecutorService logicPool() {
        return Executors.newFixedThreadPool(CORE_AVAILABLE);
    }

    public static ExecutorService logicPool(long keepAliveTime, TimeUnit unit) {
        return logicPool(CORE_AVAILABLE, keepAliveTime, unit);
    }

    public static ExecutorService logicPool(int coreAvailable, long keepAliveTime, TimeUnit unit) {
        ThreadFactory threadFactory = getThreadFactory("cpu-pool-%d");
        return new ThreadPoolExecutor(
            coreAvailable,                        // Core pool size
            coreAvailable << 1,                   // Max pool size
            keepAliveTime, unit,                        // Keep-alive time
            new LinkedBlockingQueue<>(1000),   // Queue capacity
            threadFactory
        );
    }

    public static ExecutorService ioPool() {
        return Executors.newFixedThreadPool(CORE_AVAILABLE << 1);
    }

    public static ExecutorService ioPool(int coreAvailable) {
        return Executors.newFixedThreadPool(coreAvailable << 1);
    }

    public static ExecutorService ioPool(long keepAliveTime, TimeUnit unit) {
        return ioPool(CORE_AVAILABLE, keepAliveTime, unit);
    }

    public static ExecutorService ioPool(int coreAvailable, long keepAliveTime, TimeUnit unit) {
        ThreadFactory threadFactory = getThreadFactory("io-pool-%d");
        return new ThreadPoolExecutor(
            coreAvailable << 1,                // More threads for I/O
            coreAvailable << 2,                            // Max threads
            keepAliveTime, unit,
            new LinkedBlockingQueue<>(100),
            threadFactory
        );
    }

    public static ScheduledThreadPoolExecutor scheduledPool() {
        return scheduledPool(CORE_AVAILABLE);
    }

    public static ScheduledThreadPoolExecutor scheduledPool(int coreAvailable) {
        return new ScheduledThreadPoolExecutor(coreAvailable);
    }

    public static ForkJoinPool forkJoinPool() {
        return forkJoinPool(CORE_AVAILABLE);
    }

    public static ForkJoinPool forkJoinPool(int coreAvailable) {
        return new ForkJoinPool(coreAvailable);
    }

    private static ThreadFactory getThreadFactory(String name) {
        return r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(name);
            return thread;
        };
    }
}
