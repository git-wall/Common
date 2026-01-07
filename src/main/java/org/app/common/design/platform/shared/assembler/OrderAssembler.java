package org.app.common.design.platform.shared.assembler;


import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.api.dto.OrderDetailResponse;
import org.app.common.design.platform.api.dto.OrderResponse;
import org.app.common.design.platform.domain.model.customer.Customer;
import org.app.common.design.platform.domain.model.customer.CustomerTier;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.app.common.design.platform.repo.CustomerRepository;
import org.app.common.design.platform.repo.OrderRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OrderAssembler {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public OrderDetailResponse toDetailResponse(OrderResponse orderResponse) {

        // Fetch additional data
        Customer customer = customerRepository.findById(orderResponse.getCustomerId())
            .orElseThrow();

        Order order = orderRepository.findById(orderResponse.getId())
            .orElseThrow();

        // Calculate aggregates
        BigDecimal subtotal = order.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.1"));
        BigDecimal discount = calculateDiscount(customer.getTier(), subtotal);

        // Assemble detailed response
        return OrderDetailResponse.builder()
            .orderId(order.getId())
            .orderNumber("#" + order.getId().substring(0, 8).toUpperCase())
            .customerName(customer.getName())
            .customerEmail(customer.getEmail())
            .customerTier(customer.getTier())
            .items(orderResponse.getItems())
            .subtotal(subtotal)
            .tax(tax)
            .discount(discount)
            .total(order.getTotal())
            .status(order.getStatus())
            .canCancel(order.canCancel())
            .canReturn(order.canReturn())
            .estimatedDelivery(calculateDeliveryDate(order))
            .build();
    }

    private BigDecimal calculateDiscount(CustomerTier tier, BigDecimal subtotal) {
        switch (tier) {
            case VIP:
                return subtotal.multiply(new BigDecimal("0.15"));
            case PLATINUM:
                return subtotal.multiply(new BigDecimal("0.10"));
            case GOLD:
                return subtotal.multiply(new BigDecimal("0.05"));
            default:
                return BigDecimal.ZERO;
        }
    }

    private LocalDate calculateDeliveryDate(Order order) {
        return LocalDate.now().plusDays(
            order.getStatus() == OrderStatus.CONFIRMED ? 3 : 5
        );
    }
}
