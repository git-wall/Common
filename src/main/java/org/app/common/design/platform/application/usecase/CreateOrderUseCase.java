package org.app.common.design.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.customer.Customer;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderItem;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.app.common.design.platform.domain.model.product.Product;
import org.app.common.design.platform.infrastructure.event.EventPublisher;
import org.app.common.design.platform.infrastructure.event.OrderCreatedEvent;
import org.app.common.design.platform.repo.CustomerRepository;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.design.platform.repo.ProductRepository;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.app.common.design.platform.shared.strategy.PricingStrategyRegistry;
import org.app.common.exception.business.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PricingStrategyRegistry pricingRegistry;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponse execute(OrderRequest request) {

        log.info("Creating order for customer: {}", request.getCustomerId());

        // 1. Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new NotFoundException(request.getCustomerId()));

        // 2. Create order
        Order order = Order.builder()
            .id(UUID.randomUUID().toString())
            .customerId(customer.getId())
            .status(OrderStatus.PENDING)
            .items(new ArrayList<>())
            .build();

        // 3. Add items
        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new NotFoundException(itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException(product.getId());
            }

            OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(itemReq.getQuantity())
                .price(product.getPrice())
                .build();

            order.addItem(item);
        }

        // 4. Apply pricing strategy
        var pricingStrategy = pricingRegistry.getStrategy(customer.getTier());
        BigDecimal finalPrice = pricingStrategy.apply(order.getTotal());
        order.setTotal(finalPrice);

        // 5. Save
        Order saved = orderRepository.save(order);

        // 6. Publish event
        eventPublisher.publish(OrderCreatedEvent.of(saved));

        log.info("Order created: {}", saved.getId());

        return OrderMapper.toResponse(saved);
    }
}
