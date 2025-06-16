package org.app.common.design.revisited;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LeaderFollowers<T> {
    private final BlockingQueue<T> workQueue;
    private final ExecutorService threadPool;
    private final AtomicReference<Thread> leader = new AtomicReference<>();
    private final Consumer<T> handler;

    public LeaderFollowers(int poolSize, int queueSize, Consumer<T> handler) {
        this.workQueue = new ArrayBlockingQueue<>(queueSize);
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        this.handler = handler;
    }

    public void start() {
        promoteNewLeader();
    }

    private void promoteNewLeader() {
        threadPool.submit(() -> {
            while (true) {
                Thread thread = Thread.currentThread();
                if (thread.isInterrupted()) break;
                if (leader.compareAndSet(null, thread)) {
                    try {
                        T work = workQueue.take();
                        promoteNewLeader();
                        handler.accept(work);
                    } catch (InterruptedException e) {
                        thread.interrupt();
                    } finally {
                        leader.compareAndSet(thread, null);
                    }
                }
            }
        });
    }

    public void submit(T work) throws InterruptedException {
        workQueue.put(work);
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}