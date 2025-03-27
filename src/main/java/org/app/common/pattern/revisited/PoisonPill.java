package org.app.common.pattern.revisited;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PoisonPill<T> {
    private final BlockingQueue<T> queue;
    private final T poisonPill;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public PoisonPill(BlockingQueue<T> queue, T poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
    }

    public void process(Consumer<T> consumer) {
        while (running.get()) {
            try {
                T item = queue.take();
                if (item.equals(poisonPill)) {
                    running.set(false);
                } else {
                    consumer.accept(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        try {
            queue.put(poisonPill);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}