package org.app.common.optimize;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
/**
 * // Prometheus metrics endpoint
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: prometheus,health,metrics
 *   metrics:
 *     export:
 *       prometheus:
 *         enabled: true
 * */
@Configuration
public class MonitoringConfig {
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Component
    public class PerformanceMonitor {

        @Autowired
        private MeterRegistry registry;

        public void recordDatabaseQuery(String query, long durationMs) {
            Timer.builder("db.query")
                .tag("query", query)
                .register(registry)
                .record(Duration.ofMillis(durationMs));
        }

        public void recordCacheHit(boolean hit) {
            Counter.builder("cache.hit")
                .tag("result", hit ? "hit" : "miss")
                .register(registry)
                .increment();
        }
    }

}
