package org.app.common.design.platform.infrastructure.acl;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyOrder {

    // ❌ Bad naming conventions
    private String orderNum;        // Should be orderId
    private String custId;          // Should be customerId
    private String custName;        // Customer name embedded here
    private int statusCode;         // Numeric code instead of enum
    private String paymentStatus;   // String instead of enum

    // ❌ Different data types
    private String createDate;      // String instead of LocalDateTime (format: "yyyyMMddHHmmss")
    private String totalAmt;        // String instead of BigDecimal

    // ❌ Different structure
    private List<LegacyLineItem> lineItems;  // Called "lineItems" instead of "items"

    // ❌ Extra fields we don't need
    private String legacySystemId;
    private String internalCode;
    private String customerId;
}
