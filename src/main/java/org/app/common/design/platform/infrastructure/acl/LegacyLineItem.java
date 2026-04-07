package org.app.common.design.platform.infrastructure.acl;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyLineItem {

    private String prodId;          // Product ID
    private String productName;     // Product name
    private int qty;                // Quantity
    private String price;           // String instead of BigDecimal
    private String discount;        // Discount as string

    // ❌ Legacy-specific fields
    private String warehouseCode;
    private String supplierCode;
}
