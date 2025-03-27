package org.app.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
/**
 * <pre>{@code
 * management:
 *   metrics:
 *     export:
 *       prometheus:
 *         enabled: true
 *     distribution:
 *       percentiles-histogram:
 *         http.server.requests: true
 *       sla:
 *         http.server.requests: 50ms, 100ms, 200ms
 *       percentiles:
 *         http.server.requests: 0.5, 0.9, 0.95, 0.99
 *  }</pre>
 * */
@Service
@RequiredArgsConstructor
public class Reporter implements ReportMeter {

    private final MeterRegistry registry;

    @Override
    public void timer(String name, Runnable runnable, String tags) {
        registry.timer(name, tags).record(runnable);
    }

    @Override
    public void counter(String name, String tags) {
        registry.counter(name, tags).increment();
    }

    @Override
    public void gauge(String name) {
        registry.gauge(name, 1);
    }

    public void recordServiceCall(String service, String method, long duration) {
        Timer.builder("service.calls")
                .tag("service", service)
                .tag("method", method)
                .description("Service call duration")
                .register(registry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    public void incrementErrorCount(String service, String errorType) {
        Counter.builder("service.errors")
                .tag("service", service)
                .tag("error", errorType)
                .description("Service error count")
                .register(registry)
                .increment();
    }

    public <T> T recordOperation(String name, Supplier<T> operation) {
        var sample = Timer.start(registry);
        try {
            T result = operation.get();
            sample.stop(registry.timer(name + ".success"));
            return result;
        } catch (Exception e) {
            sample.stop(registry.timer(name + ".error"));
            throw e;
        }
    }
}
