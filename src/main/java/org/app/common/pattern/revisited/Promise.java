package org.app.common.pattern.revisited;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> {
    private final CompletableFuture<T> future;

    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> Promise<T> of(T value) {
        return new Promise<>(CompletableFuture.completedFuture(value));
    }

    public <R> Promise<R> then(Function<T, R> transformation) {
        return new Promise<>(future.thenApply(transformation));
    }

    public Promise<T> onError(Consumer<Throwable> errorHandler) {
        return new Promise<>(future.exceptionally(throwable -> {
            errorHandler.accept(throwable);
            return null;
        }));
    }

    public T await() throws Exception {
        return future.get();
    }
}