package org.app.common.grpc;

import io.grpc.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class GrpcServerLifecycle implements SmartLifecycle {

    private final Server server;
    private boolean running = false;

    @Override
    public void start() {
        try {
            server.start();
            running = true;
            log.info("gRPC Server started on port {}", server.getPort());

            // Start a separate thread to await termination
            new Thread(() -> {
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                    log.error("gRPC server interrupted", e);
                }
            }).start();
        } catch (IOException e) {
            log.error("Failed to start gRPC server", e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        if (running) {
            try {
                server.shutdown().awaitTermination(30L, TimeUnit.SECONDS);
                log.info("gRPC server stopped");
            } catch (InterruptedException e) {
                log.error("Error shutting down gRPC server", e);
                server.shutdownNow();
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