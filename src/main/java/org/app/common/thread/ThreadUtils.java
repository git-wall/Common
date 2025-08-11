package org.app.common.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

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

    public static class CompileBuilder {
        public static Executor taskPool() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(CORE_AVAILABLE);
            executor.setMaxPoolSize(CORE_AVAILABLE << 1);
            executor.setQueueCapacity(500);
            executor.initialize();
            return executor;
        }

        public static ExecutorService logicPool() {
            return Executors.newFixedThreadPool(CORE_AVAILABLE);
        }

        public static ExecutorService logicPool(long keepAliveTime, TimeUnit unit) {
            return new ThreadPoolExecutor(
                    CORE_AVAILABLE,                        // Core pool size
                    CORE_AVAILABLE << 1,                   // Max pool size
                    keepAliveTime, unit,                        // Keep-alive time
                    new LinkedBlockingQueue<>(1000),   // Queue capacity
                    new ThreadFactoryBuilder()
                            .setNameFormat("cpu-pool-%d")
                            .build()
            );
        }

        public static ExecutorService ioPool() {
            return Executors.newFixedThreadPool(CORE_AVAILABLE << 1);
        }

        public static ExecutorService ioPool(long keepAliveTime, TimeUnit unit) {
            return new ThreadPoolExecutor(
                    CORE_AVAILABLE << 1,                // More threads for I/O
                    CORE_AVAILABLE << 2,                            // Max threads
                    keepAliveTime, unit,
                    new LinkedBlockingQueue<>(100),
                    new ThreadFactoryBuilder()
                            .setNameFormat("io-pool-%d")
                            .build()
            );
        }

        public static ScheduledThreadPoolExecutor scheduledPool() {
            return new ScheduledThreadPoolExecutor(CORE_AVAILABLE);
        }
    }

    public static class RuntimeBuilder {

        public static Executor taskPool() {
            int availableProcessors = getProcessors();
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(availableProcessors);
            executor.setMaxPoolSize(availableProcessors << 1);
            executor.setQueueCapacity(500);
            executor.initialize();
            return executor;
        }

        public static ExecutorService logicPool() {
            return Executors.newFixedThreadPool(getProcessors());
        }

        public static ExecutorService logicPool(long keepAliveTime, TimeUnit unit) {
            int availableProcessors = getProcessors();
            return new ThreadPoolExecutor(
                    availableProcessors,                        // Core pool size
                    availableProcessors << 1,                   // Max pool size
                    keepAliveTime, unit,                        // Keep-alive time
                    new LinkedBlockingQueue<>(1000),   // Queue capacity
                    new ThreadFactoryBuilder()
                            .setNameFormat("cpu-pool-%d")
                            .build()
            );
        }

        public static ExecutorService ioPool() {
            return Executors.newFixedThreadPool(getProcessors() << 1);
        }

        public static ThreadPoolExecutor ioPool(long keepAliveTime, TimeUnit unit) {
            int availableProcessors = getProcessors();
            return new ThreadPoolExecutor(
                    availableProcessors << 1,                // More threads for I/O
                    availableProcessors << 2,                            // Max threads
                    keepAliveTime, unit,
                    new LinkedBlockingQueue<>(100),
                    new ThreadFactoryBuilder()
                            .setNameFormat("io-pool-%d")
                            .build()
            );
        }

        public static ScheduledThreadPoolExecutor scheduledPool() {
            return new ScheduledThreadPoolExecutor(getProcessors());
        }

        public static ScheduledThreadPoolExecutor scheduledSmallPool() {
            return new ScheduledThreadPoolExecutor(2);
        }

        public static ForkJoinPool forkJoinPool() {
            return new ForkJoinPool(getProcessors());
        }

        private static int getProcessors() {
            return Runtime.getRuntime().availableProcessors();
        }

        public static void addShutdownHook(Runnable runnable) {
            Runtime.getRuntime().addShutdownHook(new Thread(runnable));
        }
    }
}
