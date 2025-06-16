package org.app.common.design.revisited;

import java.util.concurrent.*;
import java.util.function.Function;

public class HalfSyncHalfAsync<T, R> {
    private final ExecutorService asyncLayer;
    private final BlockingQueue<CompletableFuture<R>> syncQueue;

    public HalfSyncHalfAsync(int asyncThreads, int queueCapacity) {
        this.asyncLayer = Executors.newFixedThreadPool(asyncThreads);
        this.syncQueue = new ArrayBlockingQueue<>(queueCapacity);
    }

    public CompletableFuture<R> submit(T input, Function<T, R> task) {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(
            () -> task.apply(input),
            asyncLayer
        );
        syncQueue.offer(future);
        return future;
    }

    public R processSync() throws InterruptedException, ExecutionException {
        return syncQueue.take().get();
    }

    public void shutdown() {
        asyncLayer.shutdown();
    }
}