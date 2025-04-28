package org.app.common.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.grpc.GrpcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GrpcProperties.class)
@ConditionalOnProperty(prefix = "grpc.client", name = "enabled", havingValue = "true")
public class GrpcClientConfiguration {

    private final GrpcProperties grpcProperties;

    @Bean
    public ManagedChannel managedChannel() {
        GrpcProperties.Client clientProps = grpcProperties.getClient();
        log.info("Creating gRPC channel to {}:{}", clientProps.getHost(), clientProps.getPort());
        
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(
                clientProps.getHost(), 
                clientProps.getPort()
        );
        
        if (clientProps.isUsePlaintext()) {
            builder.usePlaintext();
        }
        
        if (clientProps.isKeepAliveEnabled()) {
            builder.keepAliveTime(clientProps.getKeepAliveTimeMs(), TimeUnit.MILLISECONDS)
                   .keepAliveTimeout(clientProps.getKeepAliveTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        
        if (clientProps.getMaxInboundMessageSize() > 0) {
            builder.maxInboundMessageSize(clientProps.getMaxInboundMessageSize());
        }
        
        return builder.build();
    }

    @Bean
    public GrpcClientLifecycle grpcClientLifecycle(ManagedChannel managedChannel) {
        return new GrpcClientLifecycle(managedChannel);
    }

    @Bean
    public long grpcDeadlineMs() {
        return grpcProperties.getClient().getDeadlineMs();
    }
}