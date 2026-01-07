package org.app.common.design.platform.shared.mapper;

import org.app.common.design.platform.api.dto.OrderItemResponse;
import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderItem;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order from(OrderRequest orderRequest) {
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setItems(orderRequest.getItems().stream()
            .map(itemReq -> {
                OrderItem item = new OrderItem();
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice());
                return item;
            })
            .collect(Collectors.toList()));
        order.setTotal(order.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        return order;
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .customerId(order.getCustomerId())
            .status(order.getStatus())
            .total(order.getTotal())
            .items(order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList()))
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }

    // Function for stream operations
    public static final Function<Order, OrderResponse> TO_RESPONSE = OrderMapper::toResponse;
}
