package org.app.common.design.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.app.common.design.platform.repo.OrderRepository;
import org.app.common.design.platform.shared.mapper.OrderMapper;
import org.app.common.exception.business.NotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponse getById(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(orderId));
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> getByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(OrderMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<OrderResponse> getByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(OrderMapper::toResponse)
            .collect(Collectors.toList());
    }
}
