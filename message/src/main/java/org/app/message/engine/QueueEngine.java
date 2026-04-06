package org.app.message.engine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.message.send.Sender;
import org.app.observation.log.RequestLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class QueueEngine implements Engine {

    private final BlockingQueue<RequestLog> queue = new LinkedBlockingQueue<>(10000);

    private final Sender sender;

    private final ExecutorService executor;

    private volatile boolean running = true;

    public QueueEngine(Sender sender) {
        this(sender, 1);
    }

    public QueueEngine(Sender sender, int workers) {
        this.sender = sender;

        this.executor = Executors.newFixedThreadPool(
            workers,
            r -> {
                Thread t = new Thread(r);
                t.setName("log-worker");
                t.setDaemon(true);
                return t;
            }
        );

        for (int i = 0; i < workers; i++) {
            executor.submit(this::process);
        }
    }

    @Override
    public void publish(RequestLog requestLog) {
        if (!queue.offer(requestLog)) {
            log.warn("Log queue full, dropping log");
        }
    }

    private void process() {
        int batchSize = 100;
        List<RequestLog> batch = new ArrayList<>(batchSize);

        while (running || !queue.isEmpty()) {
            try {
                // busy spin -> make CPU not loop too much and busy 100%
                RequestLog first = queue.poll(100, TimeUnit.MILLISECONDS);

                if (first != null) {
                    batch.add(first);
                    queue.drainTo(batch, batchSize - 1);
                }

                if (!batch.isEmpty()) {
                    sender.send(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Job error", e);
            }
        }
    }

    @Override
    @SneakyThrows
    public void shutdown() {
        running = false;

        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                log.warn("Log workers did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        sender.close();
    }
}
