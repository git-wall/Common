package org.app.common.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Reporter implements ReportMeter {

    private final MeterRegistry meterRegistry;

    @Override
    public void timer(String name, Runnable runnable, String tags) {
        meterRegistry.timer(name, tags).record(runnable);
    }

    @Override
    public void counter(String name, String tags) {
        meterRegistry.counter(name, tags).increment();
    }

    @Override
    public void gauge(String name) {
        meterRegistry.gauge(name, 1);
    }
}
