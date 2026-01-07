package org.app.common.design.platform.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderDetailResponse;
import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.application.usecase.CreateOrderUseCase;
import org.app.common.design.platform.application.usecase.UpdateOrderUseCase;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.pipeline.OrderProcessingPipeline;
import org.app.common.design.platform.shared.assembler.OrderAssembler;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.app.common.design.platform.shared.strategy.PaymentGatewayRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final OrderProcessingPipeline pipeline;
    private final PaymentGatewayRegistry paymentRegistry;
//    private final InventoryService inventoryService;
//    private final NotificationService notificationService;
    private final OrderAssembler orderAssembler;

    /**
     * Complex operation: Create order with payment and notification
     */
    @Transactional
    public OrderDetailResponse createCompleteOrder(OrderRequest request) {

        log.info("Starting complete order creation");

        // 1. Pipeline: Validate -> Transform -> Enrich
        Order processedData = pipeline.execute(OrderMapper.from(request));

        // 2. Create order
        OrderResponse orderResponse = createOrderUseCase.execute(
            null
//            processedData.toOrderRequest()
        );

        // 3. Reserve inventory
//        inventoryService.reserveStock(orderResponse.getItems());

        // 4. Process payment
        var paymentGateway = paymentRegistry.getGateway(request.getPaymentMethod());
//        var paymentRequest = PaymentRequest.from(orderResponse);
//        var paymentResult = paymentGateway.processPayment(paymentRequest);
//
//        if (!paymentResult.isSuccess()) {
//            inventoryService.releaseStock(orderResponse.getItems());
//            throw new RuntimeException(paymentResult.getMessage());
//        }

        // 5. Confirm order
        orderResponse = updateOrderUseCase.confirm(orderResponse.getId());

        // 6. Send notifications
//        notificationService.sendOrderConfirmation(orderResponse);

        // 7. Assemble detailed response
        return orderAssembler.toDetailResponse(orderResponse);
    }
}

