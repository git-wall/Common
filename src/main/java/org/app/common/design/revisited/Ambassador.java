package org.app.common.design.revisited;

import java.util.function.Function;
import java.util.function.Supplier;

public class Ambassador<T, R> {
    private final Supplier<T> serviceProvider;
    private final Function<T, R> operation;
    private final Function<Exception, R> fallback;
    private final int retryCount;

    public Ambassador(Supplier<T> serviceProvider, 
                     Function<T, R> operation,
                     Function<Exception, R> fallback,
                     int retryCount) {
        this.serviceProvider = serviceProvider;
        this.operation = operation;
        this.fallback = fallback;
        this.retryCount = retryCount;
    }

    public R execute() {
        Exception lastError = null;
        for (int i = 0; i < retryCount; i++) {
            try {
                return operation.apply(serviceProvider.get());
            } catch (Exception e) {
                lastError = e;
            }
        }
        return fallback.apply(lastError);
    }
}