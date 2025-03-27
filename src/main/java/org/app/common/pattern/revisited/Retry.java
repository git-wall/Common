package org.app.common.pattern.revisited;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Supplier;

public class Retry<T> {
    private final int maxAttempts;
    private final Duration delay;
    private final Class<? extends Exception>[] retryableExceptions;

    @SafeVarargs
    public Retry(int maxAttempts, Duration delay, Class<? extends Exception>... retryableExceptions) {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        this.retryableExceptions = retryableExceptions;
    }

    /**
     * Execute the given action with retries.
     * @param action the action to execute
     * @return the result of the action
     * @throws Exception if the action fails and all retries are exhausted
     */
    public T execute(Supplier<T> action) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (!isRetryable(e) || attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delay.toMillis());
            }
        }

        throw lastException;
    }

    private boolean isRetryable(Exception e) {
        return Arrays.stream(retryableExceptions).anyMatch(retryable -> retryable.isInstance(e));
    }
}