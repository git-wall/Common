package org.app.common.design.platform.api.dto;

import org.app.common.design.platform.domain.model.order.OrderStatus;

import java.time.LocalDateTime;

public class OrderSearchCriteria {
    private String customerId;
    private OrderStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }
}
