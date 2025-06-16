package org.app.common.design.revisited;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Supplier;

public class Retry<T> {
    private int RETRIES = 3;
    private Duration DELAY = Duration.ofMillis(3000L);
    private final Class<? extends Exception>[] retryableExceptions;

    @SafeVarargs
    public Retry(Class<? extends Exception>... retryableExceptions) {
        this.retryableExceptions = retryableExceptions;
    }

    @SafeVarargs
    public Retry(int retries, Duration delay, Class<? extends Exception>... retryableExceptions) {
        this.RETRIES = retries;
        this.DELAY = delay;
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

        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (!isRetryable(e) || attempt == RETRIES) {
                    throw e;
                }
                Thread.sleep(DELAY.toMillis());
            }
        }
        if (lastException == null) lastException = new Exception("Unknown error occurred");
        throw lastException;
    }

    private boolean isRetryable(Exception e) {
        return Arrays.stream(retryableExceptions).anyMatch(retryable -> retryable.isInstance(e));
    }
}