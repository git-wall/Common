package org.app.common.thread;

import org.app.common.pattern.legacy.TemplateMethod;

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
        before();
        while (hook.isRunning()) {
            now();
        }
        after();
    }
}
