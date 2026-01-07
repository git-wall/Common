package org.app.common.design.platform.api.dto;

import lombok.Builder;
import lombok.Data;
import org.app.common.design.platform.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;

    private String customerId;

    private OrderStatus status;

    private List<OrderItemResponse> items;

    private BigDecimal total;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
