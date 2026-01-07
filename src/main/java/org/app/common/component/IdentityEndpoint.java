package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
@Endpoint(id = "identity")
public class IdentityEndpoint {

    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    @ReadOperation
    public Map<String, Object> identity() {
        return Map.of(
            "instanceId", env("POD_NAME"),
            "podIp", env("POD_IP"),
            "nodeName", env("NODE_NAME"),
            "startTime", Instant.ofEpochMilli(runtimeMXBean.getStartTime()),
            "uptimeMs", runtimeMXBean.getUptime()
        );
    }

    private String env(String key) {
        return Optional.ofNullable(System.getenv(key)).orElse("unknown");
    }
}
