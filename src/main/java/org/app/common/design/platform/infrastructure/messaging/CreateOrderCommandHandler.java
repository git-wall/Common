package org.app.common.design.platform.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.application.usecase.CreateOrderUseCase;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderResponse> {

    private final CreateOrderUseCase createOrderUseCase;

    @Override
    public OrderResponse handle(CreateOrderCommand command) {
        OrderRequest request = OrderRequest.builder()
            .customerId(command.getCustomerId())
            .items(command.getItems())
            .build();

        return createOrderUseCase.execute(request);
    }
}
