package org.app.common.design.platform.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OrderRequest {
    private String customerId;
    private List<OrderItemRequest> items;
    private String paymentMethod;
}
