package org.app.common.thread;

import lombok.extern.slf4j.Slf4j;
import org.app.common.design.legacy.TemplateMethod;

/**
 * Base class for managed background threads using Template Method pattern.
 * Provides lifecycle hooks: before(), now(), after()
 * Subclasses should implement:
 * - before(): One-time initialization before main loop
 * - now(): Repeated execution in main loop
 * - after(): Cleanup after loop exits
 */
@Slf4j
public abstract class RunnableProvider extends TemplateMethod implements Runnable {

    protected ThreadHook hook;
    protected Thread thread;

    public void hook(ThreadHook hook) {
        this.hook = hook;
        this.thread = hook.getThread();
    }

    public void start() {
        thread.start();
    }

    public void setDaemon(boolean daemon) {
        thread.setDaemon(daemon);
    }

    @Override
    public void run() {
        try {
            before();
            while (hook.isRunning()) {
                try {
                    now();
                } catch (Exception e) {
                    log.error("Error in thread execution", e);
                    // Kiểm tra nếu thread bị interrupt
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }
            }
        } finally {
            after();
        }
    }
}
