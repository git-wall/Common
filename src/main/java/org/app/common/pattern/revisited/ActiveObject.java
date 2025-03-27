package org.app.common.pattern.revisited;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class ActiveObject<T> {
    private final ExecutorService executor;
    private final BlockingQueue<Future<T>> completionQueue;

    public ActiveObject(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.completionQueue = new LinkedBlockingQueue<>();
    }

    public Future<T> enqueue(Supplier<T> task) {
        Future<T> future = executor.submit(task::get);
        completionQueue.offer(future);
        return future;
    }

    public T getNextResult() throws InterruptedException, ExecutionException {
        return completionQueue.take().get();
    }

    public void shutdown() {
        executor.shutdown();
    }
}