package org.app.common.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Exception handler for gRPC services.
 */
public class GrpcExceptionHandler {

    /**
     * Convert an exception to a gRPC status exception.
     *
     * @param e The exception
     * @return A StatusRuntimeException
     */
    public static StatusRuntimeException handleException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException();
        } else if (e instanceof IllegalStateException) {
            return Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException();
        } else if (e instanceof UnsupportedOperationException) {
            return Status.UNIMPLEMENTED
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException();
        } else if (e instanceof NullPointerException) {
            return Status.INTERNAL
                    .withDescription("Unexpected null value")
                    .withCause(e)
                    .asRuntimeException();
        } else {
            return Status.UNKNOWN
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException();
        }
    }
}