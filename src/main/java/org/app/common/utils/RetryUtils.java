package org.app.common.utils;

import java.util.function.Supplier;

public class RetryUtils {
    private RetryUtils() {
        // Private constructor to prevent instantiation
    }

    public static <T> T retry(Supplier<T> operation, int maxAttempts, long initialDelayMs) {
        int attempt = 0;
        while (true) {
            try {
                return operation.get();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                sleep(initialDelayMs * (long) attempt);
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
