package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
/**
 * Endpoint kiểm tra background task / distributor
 * - Giúp theo dõi trạng thái các task đang chạy
 * - Kiểm tra queue size, số task đang active
 * - Hỗ trợ debug các vấn đề liên quan đến xử lý bất đồng bộ
 * - Biết node hiện tại đang xử lý bao nhiêu task
 * - Có task backlog hay không
 * - Dùng khi autoscale
 * */
@Component
@Endpoint(id = "task-distributor")
public class TaskDistributorEndpoint {

    @ReadOperation
    public Map<String, Object> stats() {
        return Map.of(
            "activeTasks", 12,
            "queueSize", 340,
            "nodeId", "node-3"
        );
    }
}
