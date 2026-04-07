package org.app.common.design.platform.domain.specification;

import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> isConfirmed() {
        return order -> order.getStatus() == OrderStatus.CONFIRMED;
    }

    public static Specification<Order> hasMinimumTotal(BigDecimal minTotal) {
        return order -> order.getTotal().compareTo(minTotal) >= 0;
    }

    public static Specification<Order> isRecent(int days) {
        return order -> order.getCreatedAt()
            .isAfter(LocalDateTime.now().minusDays(days));
    }

    public static Specification<Order> canBeCancelled() {
        return Order::canCancel;
    }

    // Usage
    public static Specification<Order> highValueRecentOrder() {
        return isConfirmed()
            .and(hasMinimumTotal(new BigDecimal("1000")))
            .and(isRecent(7));
    }
}
