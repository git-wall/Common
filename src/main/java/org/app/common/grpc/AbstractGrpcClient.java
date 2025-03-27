package org.app.common.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for gRPC clients.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGrpcClient {

    protected final ManagedChannel channel;
    protected final long deadlineMs;

    /**
     * Execute a gRPC call with exception handling.
     *
     * @param operation The operation to execute
     * @param <T> The return type
     * @return The result of the operation
     * @throws GrpcClientException If an error occurs
     */
    protected <T> T executeWithRetry(GrpcOperation<T> operation) throws GrpcClientException {
        try {
            return operation.execute();
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed: {}", e.getMessage());
            throw new GrpcClientException("gRPC call failed", e);
        } catch (Exception e) {
            log.error("Error in gRPC client", e);
            throw new GrpcClientException("Error in gRPC client", e);
        }
    }

    /**
     * Functional interface for gRPC operations.
     *
     * @param <T> The return type
     */
    @FunctionalInterface
    public interface GrpcOperation<T> {
        T execute() throws Exception;
    }
}