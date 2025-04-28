package org.app.common.grpc;

import org.app.common.grpc.client.EnableGrpcClient;
import org.app.common.grpc.interceptor.LoggingInterceptor;
import org.app.common.grpc.interceptor.MetricsInterceptor;
import org.app.common.grpc.interceptor.SecurityInterceptor;
import org.app.common.grpc.interceptor.ValidationInterceptor;
import org.app.common.grpc.server.EnableGrpcServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for gRPC.
 */
@Configuration
@EnableGrpcServer
@EnableGrpcClient
@ComponentScan("org.app.common.grpc")
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ValidationInterceptor validationInterceptor() {
        return new ValidationInterceptor();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "grpc.auth", name = "enabled", havingValue = "true")
    public SecurityInterceptor securityInterceptor(GrpcProperties grpcProperties) {
        return new SecurityInterceptor(grpcProperties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MetricsInterceptor metricsInterceptor() {
        return new MetricsInterceptor();
    }
}