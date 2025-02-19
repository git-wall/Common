package org.app.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheHealthCheck implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public CacheHealthCheck(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try {
            // Ping the Redis server to check connectivity
            String response = redisConnectionFactory.getConnection().ping();
            if ("PONG".equals(response)) {
                return Health.up().withDetail("message", "Cache is reachable").build();
            } else {
                return Health.down().withDetail("error", "Unexpected response from cache").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}