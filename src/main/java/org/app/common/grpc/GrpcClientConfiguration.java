package org.app.common.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GrpcClientConfiguration {

    @Value("${grpc.client.host:localhost}")
    private String host;

    @Value("${grpc.client.port:9090}")
    private int port;

    @Value("${grpc.client.deadline:5000}")
    private long deadlineMs;

    @Bean
    public ManagedChannel managedChannel() {
        log.info("Creating gRPC channel to {}:{}", host, port);
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public GrpcClientLifecycle grpcClientLifecycle(ManagedChannel managedChannel) {
        return new GrpcClientLifecycle(managedChannel);
    }

    @Bean
    public long grpcDeadlineMs() {
        return deadlineMs;
    }
}