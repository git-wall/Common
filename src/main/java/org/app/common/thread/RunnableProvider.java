package org.app.common.thread;

/**
 * @apiNote
 * <pre>{@code
 *     @AutoRun
 *     public class ScheduledReport extends RunnableProvider {
 *         @Override
 *         public void before() {
 *             logg(this.getClass().getSimpleName(), " ready to run");
 *         }
 *
 *         @Override
 *         public void now() {
 *         }
 *
 *         @Override
 *         public void after() {
 *             logg(this.getClass().getSimpleName(), " close");
 *         }
 *     }
 * }</pre>
 * */
public abstract class RunnableProvider implements Runnable {

    private ThreadHook hook;
    private Thread thread;

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
        while (true) {
            boolean running = hook.isRunning();
            if (!running) break;
            now();
        }
        after();
    }

    public abstract void before();

    public abstract void now();

    public abstract void after();
}
