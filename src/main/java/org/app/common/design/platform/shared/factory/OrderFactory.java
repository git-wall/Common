package org.app.common.design.platform.shared.factory;

import org.app.common.design.platform.api.dto.OrderRequest;
import org.app.common.design.platform.domain.model.customer.Customer;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class OrderFactory {

    public Order createDraft(String customerId) {
        return Order.builder()
            .id(UUID.randomUUID().toString())
            .customerId(customerId)
            .status(OrderStatus.DRAFT)
            .items(new ArrayList<>())
            .total(BigDecimal.ZERO)
            .build();
    }

    public Order createFromRequest(OrderRequest request, Customer customer) {
        return Order.builder()
            .id(UUID.randomUUID().toString())
            .customerId(customer.getId())
            .status(OrderStatus.PENDING)
            .items(new ArrayList<>())
            .build();
    }
}
