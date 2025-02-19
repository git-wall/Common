package org.app.common.thread;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ThreadHook {
    private final Thread thread;
    private boolean running = true;

    public ThreadHook(Thread thread) {
        this.thread = thread;
    }

    public void shutdown() {
        running = false;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Error shutting down thread with hook", e);
            Thread.currentThread().interrupt();
        } catch (ThreadDeath e) {
            log.warn("Interrupted!", e);
            throw e;
        }
    }

}
