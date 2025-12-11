package org.app.common.design.revisited.retry;

import java.time.Duration;
import java.util.Arrays;

public class Retry<T> {
    private int retries = 3;
    private Duration duration = Duration.ofMillis(3000L);
    private final Class<? extends Throwable>[] retryableExceptions;

    @SafeVarargs
    public Retry(Class<? extends Throwable>... retryableExceptions) {
        this.retryableExceptions = retryableExceptions;
    }

    @SafeVarargs
    public Retry(int retries, Duration duration, Class<? extends Throwable>... retryableExceptions) {
        this.retries = retries;
        this.duration = duration;
        this.retryableExceptions = retryableExceptions;
    }

    /**
     * Execute the given action with retries.
     *
     * @param action the action to execute
     * @return the result of the action
     * @throws Exception if the action fails and all retries are exhausted
     */
    public T execute(ThrowableSupplier<T> action) throws Throwable {
        Throwable lastException = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                return action.get();
            } catch (Throwable e) {
                lastException = e;
                if (!isRetryable(e) || attempt == retries) {
                    throw e;
                }
                Thread.sleep(duration.toMillis());
            }
        }
        if (lastException == null) lastException = new Exception("Unknown error occurred");
        throw lastException;
    }

    private boolean isRetryable(Throwable e) {
        return Arrays.stream(retryableExceptions).anyMatch(retryable -> retryable.isInstance(e));
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }
}
