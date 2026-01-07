package org.app.common.thread;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A service that manages thread hooks and ensures proper shutdown of threads during application termination.
 * Implements {@link ApplicationRunner} to register tasks at application startup and {@link DisposableBean}
 * to handle cleanup during shutdown.
 */
@Service
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class ThreadHookService implements ApplicationRunner, DisposableBean {

    private final List<ThreadHook> hooks;
    private Thread shutdownHookThread;
    private volatile boolean shutdownExecuted = false;

    public ThreadHookService() {
        hooks = new ArrayList<>();
        addShutdownHook();
    }

    protected void addShutdownHook() {
        shutdownHookThread = new ShutdownDaemonHook();
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
    }

    @Override
    public void destroy() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
            log.debug("Removed JVM shutdown hook (Spring handling shutdown)");
        } catch (IllegalStateException e) {
            log.debug("Cannot remove hook, JVM already shutting down");
        }

        executeShutdown();
    }

    private void executeShutdown() {
        if (shutdownExecuted) {
            log.warn("Shutdown already executed, skipping");
            return;
        }
        shutdownExecuted = true;

        log.info("_____________Running shutdown hooks_____________");
        for (ThreadHook hook : hooks) {
            log.info("Shutting down thread: {}", hook.getThread().getName());
            hook.shutdown();
        }
        hooks.clear();
    }

    public class ShutdownDaemonHook extends Thread {
        @Override
        public void run() {
            executeShutdown();
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        ApplicationContext context = SpringContext.getContext();
        String[] beanNames = context.getBeanNamesForAnnotation(AutoRun.class);
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            if (extracted(bean)) {
                log.info("Register task runnable: {}", beanName);
            }
        }
    }

    private boolean extracted(Object bean) {
        if (bean instanceof RunnableProvider) {
            RunnableProvider runnable = (RunnableProvider) bean;
            Thread thread = new Thread(runnable);
            ThreadHook hook = new ThreadHook(thread);
            hooks.add(hook);
            runnable.hook(hook);
            runnable.setDaemon(true);
            runnable.start();
            return true;
        }
        return false;
    }
}
