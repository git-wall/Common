package org.app.common.design.platform.infrastructure.messaging;

import lombok.Builder;
import lombok.Value;
import org.app.common.design.platform.api.dto.OrderItemRequest;
import org.app.common.design.platform.api.dto.OrderRequest;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class CreateOrderCommand implements Command {
    String commandId;
    String customerId;
    List<OrderItemRequest> items;

    public static CreateOrderCommand of(OrderRequest request) {
        return CreateOrderCommand.builder()
            .commandId(UUID.randomUUID().toString())
            .customerId(request.getCustomerId())
            .items(request.getItems())
            .build();
    }
}
