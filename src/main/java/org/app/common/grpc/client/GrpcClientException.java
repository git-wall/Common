package org.app.common.grpc.client;

/**
 * Exception thrown by gRPC clients.
 */
public class GrpcClientException extends RuntimeException {

    private static final long serialVersionUID = 3402441812173143039L;

    public GrpcClientException(String message) {
        super(message);
    }
    
    public GrpcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}