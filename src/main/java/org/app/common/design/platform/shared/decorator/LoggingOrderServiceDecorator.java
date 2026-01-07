package org.app.common.design.platform.shared.decorator;

import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.application.usecase.CreateOrderUseCase;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingOrderServiceDecorator {

    private final CreateOrderUseCase decorated;

    public LoggingOrderServiceDecorator(CreateOrderUseCase decorated) {
        this.decorated = decorated;
    }

    public OrderResponse execute(OrderRequest request) {
        log.info("=== Starting order creation ===");
        log.info("Customer: {}", request.getCustomerId());
        log.info("Items count: {}", request.getItems().size());

        long startTime = System.currentTimeMillis();

        try {
            OrderResponse response = decorated.execute(request);

            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Order created successfully in {}ms ===", duration);
            log.info("Order ID: {}", response.getId());

            return response;
        } catch (Exception e) {
            log.error("=== Order creation failed ===", e);
            throw e;
        }
    }
}
