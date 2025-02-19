package org.app.common.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class ThreadConfig {

    private static final int CORE_AVAILABLE = Runtime.getRuntime().availableProcessors();

    public ExecutorService createCpuBoundPool(long keepAliveTime,
                                              TimeUnit unit) {
        return new ThreadPoolExecutor(
                CORE_AVAILABLE,                             // Core pool size
                CORE_AVAILABLE << 1,                        // Max pool size
                keepAliveTime, unit,                  // Keep-alive time
                new LinkedBlockingQueue<>(1000),// Queue capacity
                new ThreadFactoryBuilder()
                        .setNameFormat("cpu-pool-%d")
                        .build()
        );
    }

    public ExecutorService createIoBoundPool(long keepAliveTime,
                                             TimeUnit unit) {
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

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_AVAILABLE);
        executor.setMaxPoolSize(CORE_AVAILABLE << 1);
        executor.setQueueCapacity(500);
        executor.initialize();
        return executor;
    }

    @Bean("logicThreadPool")
    public ExecutorService logicThreadPool() {
        return Executors.newFixedThreadPool(CORE_AVAILABLE);
    }

    @Bean("ioThreadPool")
    public ExecutorService ioThreadPool() {
        return Executors.newFixedThreadPool(CORE_AVAILABLE << 1);
    }

    @Bean("workerThreadPool")
    public ExecutorService schedulingThreadPool() {
        return Executors.newScheduledThreadPool(2);
    }
}
