package org.app.common.design.platform.api.dto;

import lombok.Builder;
import org.app.common.design.platform.domain.model.customer.CustomerTier;
import org.app.common.design.platform.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public class OrderDetailResponse {
    private String orderId;
        private String orderNumber;
        private String customerName;
        private String customerEmail;
        private CustomerTier customerTier;
        private List<OrderItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal discount;
        private BigDecimal total;
        private OrderStatus status;
        private boolean canCancel;
        private boolean canReturn;
        private LocalDate estimatedDelivery;
}
