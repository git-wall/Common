package org.app.common.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ThreadHookService implements ApplicationRunner, DisposableBean {

    private final List<ThreadHook> hooks;

    private final ApplicationContext context;

    @Autowired
    public ThreadHookService(ApplicationContext context) {
        this.context = context;
        hooks = new ArrayList<>();
        addShutdownHook();
    }

    protected void addShutdownHook() {
        ShutdownDaemonHook shutdownHook = new ShutdownDaemonHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void destroy() {
        new ShutdownDaemonHook().start();
    }

    public class ShutdownDaemonHook extends Thread {
        @Override
        public void run() {
            log.info("_____________Running shutdown hooks_____________");
            for (ThreadHook hook : hooks) {
                hook.shutdown();
            }
            hooks.clear();
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] beanNames = context.getBeanNamesForAnnotation(AutoRun.class);
        for (String beanName : beanNames) {
            extracted(beanName);
        }
    }

    private void extracted(String beanName) {
        Object bean = context.getBean(beanName);
        if (bean instanceof RunnableProvider) {
            RunnableProvider runnable = (RunnableProvider) bean;
            Thread thread = new Thread(runnable);
            ThreadHook hook = new ThreadHook(thread);
            hooks.add(hook);
            runnable.hook(hook);
            runnable.setDaemon(true);
            runnable.start();
            log.info("Register task runnable: {}", beanName);
        }
    }
}
