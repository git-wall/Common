package org.app.common.thread;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread lifecycle management with proper shutdown handling.
 * Thread-safe and follows best practices for graceful termination.
 */
@Getter
@Slf4j
public class ThreadHook {
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ThreadHook(Thread thread) {
        this.thread = thread;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void shutdown() {
        running.set(false);
        thread.interrupt();
        try {
            thread.join(30000); // 30 seconds timeout
            log.info("Thread {} terminated gracefully", thread.getName());
        } catch (InterruptedException e) {
            log.error("Interrupted while shutting down thread: {}", thread.getName(), e);
            Thread.currentThread().interrupt();
        }

        // Check if thread still alive after timeout
        if (thread.isAlive()) {
            log.error("Thread {} still alive after 30s timeout", thread.getName());
        }
    }
}
