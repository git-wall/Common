package org.app.common.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcServerConfiguration {

    @Value("${grpc.server.port:9090}")
    private int port;

    private final List<GrpcServiceDefinition> serviceDefinitions;
    private final List<ServerInterceptor> interceptors;

    @Bean
    public Server grpcServer() {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);

        // Register all service implementations
        for (GrpcServiceDefinition serviceDefinition : serviceDefinitions) {
            serverBuilder.addService(serviceDefinition.getServiceDefinition());
            log.info("Registered gRPC service: {}", serviceDefinition.getServiceName());
        }

        // Add all interceptors
        for (ServerInterceptor interceptor : interceptors) {
            serverBuilder.intercept(interceptor);
            log.info("Added gRPC interceptor: {}", interceptor.getClass().getSimpleName());
        }

        return serverBuilder.build();
    }

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(Server grpcServer) {
        return new GrpcServerLifecycle(grpcServer);
    }
}