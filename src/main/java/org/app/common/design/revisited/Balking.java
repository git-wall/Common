package org.app.common.design.revisited;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Balking<T> {
    private final AtomicBoolean jobInProgress = new AtomicBoolean(false);

    public T execute(Supplier<T> job) {
        if (jobInProgress.compareAndSet(false, true)) {
            try {
                return job.get();
            } finally {
                jobInProgress.set(false);
            }
        }
        return null;
    }

    public boolean isJobInProgress() {
        return jobInProgress.get();
    }
}