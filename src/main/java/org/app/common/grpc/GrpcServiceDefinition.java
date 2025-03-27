package org.app.common.grpc;

import io.grpc.ServerServiceDefinition;

/**
 * Interface for gRPC service definitions.
 * Implementations of this interface will be automatically registered with the gRPC server.
 */
public interface GrpcServiceDefinition {
    
    /**
     * Get the gRPC service definition.
     * 
     * @return The server service definition
     */
    ServerServiceDefinition getServiceDefinition();
    
    /**
     * Get the name of the service.
     * 
     * @return The service name
     */
    String getServiceName();
}