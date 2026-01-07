package org.app.common.design.platform.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemResponse {
    private String id;

    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

}
