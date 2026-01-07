package org.app.common.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * Endpoint theo dõi Redis / Cache trạng thái runtime <br>
 * - Biết app đang connect master hay replica <br>
 * - Cache hit/miss <br>
 * - Tránh phải SSH vào server
 */
@Component
@Endpoint(id = "cache-status")
@RequiredArgsConstructor
public class CacheStatusEndpoint {

    private final StringRedisTemplate redisTemplate;

    @ReadOperation
    public Map<String, Object> redis() {
        return redisTemplate.execute((RedisCallback<Map<String, Object>>) con -> {
            Properties info = con.info("replication");

            return Map.of(
                "role", info.getProperty("role"),
                "connectedSlaves", info.getProperty("connected_slaves"),
                "masterHost", info.getProperty("master_host")
            );
        });
    }
}
