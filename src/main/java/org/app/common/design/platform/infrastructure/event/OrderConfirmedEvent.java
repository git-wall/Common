package org.app.common.design.platform.infrastructure.event;
import lombok.Builder;
import lombok.Value;
import org.app.common.design.platform.domain.model.order.Order;

import java.time.LocalDateTime;
@Value
@Builder
public class OrderConfirmedEvent implements OrderEvent {
    String orderId;
    LocalDateTime occurredAt;

    public static OrderConfirmedEvent of(Order order) {
        return new OrderConfirmedEvent(order.getId(), LocalDateTime.now());
    }
}
