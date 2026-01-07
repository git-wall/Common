package org.app.common.design.platform.infrastructure.event;

import lombok.Builder;
import lombok.Value;
import org.app.common.design.platform.domain.model.order.Order;

import java.time.LocalDateTime;
@Value
@Builder
public class OrderShippedEvent implements OrderEvent {
    String orderId;
    LocalDateTime occurredAt;

    public static OrderShippedEvent of(Order order) {
        return new OrderShippedEvent(order.getId(), LocalDateTime.now());
    }
}
