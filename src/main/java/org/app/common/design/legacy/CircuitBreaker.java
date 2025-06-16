package org.app.common.design.legacy;

import java.util.function.Supplier;

public class CircuitBreaker {

    private static final int FAILURE_THRESHOLD = 3;
    private int failureCount = 0;
    private boolean isOpen = false;

    public <T> T execute(Supplier<T> supplier) throws Exception {
        if (isOpen) {
            throw new Exception("Circuit is open");
        }

        try {
            T result = supplier.get();
            reset();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }

    private void recordFailure() {
        failureCount++;
        if (failureCount >= FAILURE_THRESHOLD) {
            isOpen = true;
        }
    }

    private void reset() {
        failureCount = 0;
        isOpen = false;
    }
}