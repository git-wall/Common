package org.app.common.redis;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class RedisAsyncExecutor {

    private final ExecutorService executor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors() / 2)
    );

    public <R> CompletableFuture<R> submit(Supplier<R> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public CompletableFuture<Void> submitVoid(Runnable action) {
        return CompletableFuture.runAsync(action, executor);
    }

    public <R> List<R> batch(List<Supplier<R>> tasks) {
        try {
            List<CompletableFuture<R>> futures = tasks.stream()
                .map(t -> CompletableFuture.supplyAsync(t, executor))
                .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Batch execution error", e);
        }
    }
}
