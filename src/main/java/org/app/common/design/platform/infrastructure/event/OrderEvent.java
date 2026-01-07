package org.app.common.design.platform.infrastructure.event;

import java.time.LocalDateTime;

public interface OrderEvent {
    String getOrderId();
    LocalDateTime getOccurredAt();
}
