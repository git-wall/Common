package org.app.common.grpc;

/**
 * Exception thrown by gRPC clients.
 */
public class GrpcClientException extends RuntimeException {
    
    public GrpcClientException(String message) {
        super(message);
    }
    
    public GrpcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}