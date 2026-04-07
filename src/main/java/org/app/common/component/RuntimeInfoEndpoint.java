package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Map;

/**
 * Endpoint phục vụ incident response (Read-only) <br>
 * - Khi production đang cháy 🔥 <br>
 * - Current node time <br>
 * - Timezone <br>
 * - JVM uptime <br>
 * - Active profile
 */
@Component
@Endpoint(id = "runtime-info")
public class RuntimeInfoEndpoint {

    @ReadOperation
    public Map<String, Object> runtime() {
        return Map.of(
            "profile", System.getProperty("spring.profiles.active"),
            "timezone", ZoneId.systemDefault().toString(),
            "jvmVersion", System.getProperty("java.version"),
            "processors", Runtime.getRuntime().availableProcessors(),
            "usedMemoryMb",
            (Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()) / 1024 / 1024
        );
    }
}

