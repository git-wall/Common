package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Endpoint cung cấp thông tin build của ứng dụng <br>
 * - Giúp theo dõi phiên bản đang chạy trên các môi trường <br>
 * - Hỗ trợ debug khi có sự cố liên quan đến phiên bản <br>
 * - Cung cấp thông tin về commit, thời gian build
 * */
@Component
@Endpoint(id = "build-info")
public class BuildInfoEndpoint {

    // try -> /actuator/build-info
    @ReadOperation
    public Map<String, Object> buildInfo() {
        return Map.of(
            "app", "campaign-service",
            "version", "1.4.3",
            "gitCommit", "a8f2c91",
            "buildTime", "2025-12-20T10:15:00Z"
        );
    }
}
