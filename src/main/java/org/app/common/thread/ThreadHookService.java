package org.app.common.thread;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Constructor that initializes the list of thread hooks and adds a shutdown hook to the JVM runtime.
     */
    @Autowired
    public ThreadHookService() {
        hooks = new ArrayList<>();
        addShutdownHook();
    }

    /**
     * Adds a shutdown hook to the JVM runtime to ensure proper cleanup of threads during application termination.
     */
    protected void addShutdownHook() {
        ShutdownDaemonHook shutdownHook = new ShutdownDaemonHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Invoked during application shutdown to start the shutdown daemon hook, which handles thread cleanup.
     */
    @Override
    public void destroy() {
        new ShutdownDaemonHook().start();
    }

    /**
     * A daemon thread that runs during application shutdown to execute all registered thread hooks.
     */
    public class ShutdownDaemonHook extends Thread {
        @Override
        public void run() {
            log.info("_____________Running shutdown hooks_____________");
            for (ThreadHook hook : hooks) {
                log.info("Shutting down thread: {}", hook.getThread().getName());
                hook.shutdown();
            }
            hooks.clear();
        }
    }

    /**
     * Invoked at application startup to register tasks annotated with {@code @AutoRun}.
     *
     * @param args The application arguments passed during startup.
     */
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

    /**
     * Extracts and registers a runnable task if the provided bean implements {@link RunnableProvider}.
     *
     * @param bean The bean to check and register.
     * @return {@code true} if the bean was registered as a runnable task, {@code false} otherwise.
     */
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
