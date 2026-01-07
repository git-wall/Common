package org.app.common.design.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.infrastructure.event.EventPublisher;
import org.app.common.design.platform.infrastructure.event.OrderCancelledEvent;
import org.app.common.design.platform.infrastructure.event.OrderConfirmedEvent;
import org.app.common.design.platform.infrastructure.event.OrderShippedEvent;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.app.common.exception.business.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderUseCase {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse confirm(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(orderId));

        order.confirm();
        Order saved = orderRepository.save(order);

        eventPublisher.publish(OrderConfirmedEvent.of(saved));

        return OrderMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse ship(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(orderId));

        order.ship();
        Order saved = orderRepository.save(order);

        eventPublisher.publish(OrderShippedEvent.of(saved));

        return OrderMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse cancel(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(orderId));

        order.cancel();
        Order saved = orderRepository.save(order);

        eventPublisher.publish(OrderCancelledEvent.of(saved));

        return OrderMapper.toResponse(saved);
    }
}

