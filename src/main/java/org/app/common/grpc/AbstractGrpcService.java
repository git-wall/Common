
package org.app.common.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;


/**
 * Base class for gRPC service implementations.
 *
 * @param <T> The type of the gRPC service
 */

@Slf4j
public abstract class AbstractGrpcService<T> implements GrpcServiceDefinition {


    /**
     * Handle exceptions in gRPC method calls.
     *
     * @param e                The exception
     * @param responseObserver The response observer
     * @param <R>              The response type
     */

    protected <R> void handleException(Exception e, StreamObserver<R> responseObserver) {
        log.error("Error in gRPC service call", e);
        responseObserver.onError(GrpcExceptionHandler.handleException(e));
    }

    /**
     * Complete a gRPC call with a response.
     *
     * @param response         The response
     * @param responseObserver The response observer
     * @param <R>              The response type
     */

    protected <R> void complete(R response, StreamObserver<R> responseObserver) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
