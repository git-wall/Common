package org.app.common.grpc.client;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class GrpcClientLifecycle implements SmartLifecycle {

    private final ManagedChannel channel;
    private boolean running = false;

    @Override
    public void start() {
        running = true;
        log.info("gRPC client channel started");
    }

    @Override
    public void stop() {
        if (running) {
            try {
                log.info("Shutting down gRPC client channel");
                channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error shutting down gRPC client channel", e);
                channel.shutdownNow();
            } finally {
                running = false;
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}