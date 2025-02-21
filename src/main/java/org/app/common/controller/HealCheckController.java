package org.app.common.controller;

import lombok.RequiredArgsConstructor;
import org.app.common.health.CacheHealthCheck;
import org.app.common.health.DbHealthCheck;
import org.app.common.health.SystemHealthCheck;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealCheckController {

    private final CacheHealthCheck cacheHealthCheck;

    private final DbHealthCheck dbHealthCheck;

    private final SystemHealthCheck systemHealthCheck;

    @GetMapping("/health")
    public Health health() {
        return Health.up()
                .withDetail("heap", Runtime.getRuntime().freeMemory())
                .withDetail("threads", Thread.activeCount())
                .build();
    }

    @GetMapping("/health/liveness")
    public Health liveness() {
        return Health.up().build();  // Basic health check
    }

    @GetMapping("/health/readiness")
    public Health readiness() {
        return Health.up()
                .withDetail("db", dbHealthCheck.health())
                .withDetail("cache", cacheHealthCheck.health())
                .build();
    }

    @GetMapping("/health/system")
    public Health system() {
        return systemHealthCheck.health();
    }
}
