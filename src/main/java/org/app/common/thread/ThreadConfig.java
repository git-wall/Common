package org.app.common.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.app.common.thread.ThreadUtils.CompileBuilder.*;

@Configuration
@EnableAsync
public class ThreadConfig {

    @Bean
    public Executor taskExecutor() {
        return taskPool("app-thread-", null);
    }

    // Core pool size
    @Bean("logicThreadPool")
    public ExecutorService logicThreadPool() {
        return logicPool();
    }

    // More threads for I/O
    @Bean("ioThreadPool")
    public ExecutorService ioThreadPool() {
        return ioPool();
    }

    @Bean("workerThreadPool")
    public ScheduledExecutorService schedulingThreadPool() {
        return scheduledPool();
    }
}
