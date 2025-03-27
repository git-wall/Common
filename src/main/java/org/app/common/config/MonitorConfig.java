package org.app.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorConfig {

    @Value("${spring.application.name}")
    private String application;

    // Metrics for HPA
    // implementation 'org.springframework.boot:spring-boot-starter-actuator'
    // implementation 'io.micrometer:micrometer-registry-prometheus'
    // management.endpoints.web.exposure.include=prometheus
//    @Bean
//    public MeterRegistry meterRegistry() {
//        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
//        registry.config().commonTags("app", application);
//        return registry;
//    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("app", application);
    }
}
