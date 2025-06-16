package org.app.common.design.revisited;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int failureThreshold;
    private long lastFailureTime;

    public CircuitBreaker(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public <T> T execute(CircuitBreakerOperation<T> operation) throws Exception {
        if (state.get() == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > 5000L) {
                state.set(State.HALF_OPEN);
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker is open");
            }
        }

        try {
            T result = operation.execute();
            reset();
            return result;
        } catch (Exception e) {
            handleFailure();
            throw e;
        }
    }

    private void handleFailure() {
        lastFailureTime = System.currentTimeMillis();
        if (failureCount.incrementAndGet() >= failureThreshold) {
            state.set(State.OPEN);
        }
    }

    private void reset() {
        failureCount.set(0);
        state.set(State.CLOSED);
    }

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        private static final long serialVersionUID = 666318998450530040L;

        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}