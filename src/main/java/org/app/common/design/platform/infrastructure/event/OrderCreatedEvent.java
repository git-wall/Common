package org.app.common.design.platform.infrastructure.event;

import lombok.Builder;
import lombok.Value;
import org.app.common.design.platform.domain.model.order.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderCreatedEvent implements OrderEvent {
    String orderId;
    String customerId;
    BigDecimal total;
    LocalDateTime occurredAt;

    public static OrderCreatedEvent of(Order order) {
        return OrderCreatedEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .total(order.getTotal())
            .occurredAt(LocalDateTime.now())
            .build();
    }
}
