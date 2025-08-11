package org.app.common.metrics.micrometer;

import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import org.springframework.context.annotation.Bean;

public class MicrometerApp {
    @Bean
    JvmThreadMetrics threadMetrics() {
        return new JvmThreadMetrics();
    }
}
