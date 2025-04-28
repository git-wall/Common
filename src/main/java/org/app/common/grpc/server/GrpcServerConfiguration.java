package org.app.common.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.grpc.GrpcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GrpcProperties.class)
@ConditionalOnProperty(prefix = "grpc.server", name = "enabled", havingValue = "true")
public class GrpcServerConfiguration {

    private final GrpcProperties grpcProperties;
    private final List<GrpcServiceDefinition> serviceDefinitions;
    private final List<ServerInterceptor> interceptors;

    @Bean
    public Server grpcServer() {
        GrpcProperties.Server serverProps = grpcProperties.getServer();
        int port = serverProps.getPort();
        
        log.info("Configuring gRPC server on port {}", port);
        
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        
        // Add all service definitions
        if (serviceDefinitions != null) {
            for (GrpcServiceDefinition definition : serviceDefinitions) {
                log.info("Registering gRPC service: {}", definition.getServiceName());
                serverBuilder.addService(definition.getServiceDefinition());
            }
        }
        
        // Add all interceptors
        if (interceptors != null) {
            for (ServerInterceptor interceptor : interceptors) {
                log.info("Adding gRPC interceptor: {}", interceptor.getClass().getSimpleName());
                serverBuilder.intercept(interceptor);
            }
        }
        
        // Configure max message sizes
        if (serverProps.getMaxInboundMessageSize() > 0) {
            serverBuilder.maxInboundMessageSize(serverProps.getMaxInboundMessageSize());
        }
        
        if (serverProps.getMaxInboundMetadataSize() > 0) {
            serverBuilder.maxInboundMetadataSize(serverProps.getMaxInboundMetadataSize());
        }
        
        return serverBuilder.build();
    }

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(Server grpcServer) {
        return new GrpcServerLifecycle(grpcServer);
    }
}