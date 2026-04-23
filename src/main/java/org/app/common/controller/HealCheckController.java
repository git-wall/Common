package org.app.common.controller;

import lombok.RequiredArgsConstructor;
import org.app.common.constant.TagURL;
import org.app.common.health.CacheHealthCheck;
import org.app.common.health.DbHealthCheck;
import org.app.common.health.SystemHealthCheck;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealCheckController {

    private final CacheHealthCheck cacheHealthCheck;

    private final DbHealthCheck dbHealthCheck;

    private final SystemHealthCheck systemHealthCheck;

    @GetMapping(value = TagURL.HEALTH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Health health() {
        return Health.up()
                .withDetail("heap", Runtime.getRuntime().freeMemory())
                .withDetail("threads", Thread.activeCount())
                .build();
    }

    @GetMapping(TagURL.HEALTH_LIVE)
    public Health liveness() {
        return Health.up().build();  // Basic health check
    }

    @GetMapping(TagURL.HEALTH_READY)
    public Health readiness() {
        return Health.up()
                .withDetail("db", dbHealthCheck.health())
                .withDetail("cache", cacheHealthCheck.health())
                .build();
    }

    @GetMapping(TagURL.HEALTH_SYSTEM)
    public Health system() {
        return systemHealthCheck.health();
    }

}
