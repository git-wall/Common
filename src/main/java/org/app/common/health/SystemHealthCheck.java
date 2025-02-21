package org.app.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SystemHealthCheck implements HealthIndicator {

    @Override
    public Health health() {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        int activeThreads = Thread.activeCount();

        return Health.up()
                .withDetail("freeMemory", freeMemory)
                .withDetail("totalMemory", totalMemory)
                .withDetail("activeThreads", activeThreads)
                .build();
    }
}
