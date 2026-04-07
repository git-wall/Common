package org.app.common.design.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private String productId;
    private int quantity;
    private String productName;
    private BigDecimal price;
}
