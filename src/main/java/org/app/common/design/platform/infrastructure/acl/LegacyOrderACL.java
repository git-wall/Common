package org.app.common.design.platform.infrastructure.acl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.domain.model.customer.Customer;
import org.app.common.design.platform.domain.model.customer.CustomerTier;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderItem;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.app.common.design.platform.domain.model.product.Product;
import org.app.common.design.platform.repo.CustomerRepository;
import org.app.common.design.platform.repo.ProductRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LegacyOrderACL {

    private final LegacySystemClient legacyClient;  // ← Client gọi API bên ngoài
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /**
     * IMPORT từ Legacy System → Domain Model
     * <p>
     * Flow:
     * 1. Fetch data từ legacy via LegacySystemClient
     * 2. Validate legacy data
     * 3. Translate legacy format → domain format
     * 4. Enrich với data từ DB nếu cần
     * 5. Validate domain model
     * 6. Return clean domain model
     */
    public Order importFromLegacy(String legacyOrderId) {

        log.info("=== Importing order from legacy system ===");
        log.info("Legacy Order ID: {}", legacyOrderId);

        // STEP 1: Fetch từ legacy system
        LegacyOrder legacyOrder = legacyClient.getOrder(legacyOrderId);

        // STEP 2: Validate legacy data (protect domain)
        validateLegacyData(legacyOrder);

        // STEP 3: Translate legacy → domain
        Order order = translateToDomain(legacyOrder);

        // STEP 4: Enrich với data từ DB
        enrichOrder(order);

        // STEP 5: Validate domain model
        validateDomainOrder(order);

        log.info("=== Successfully imported order: {} ===", order.getId());

        return order;
    }

    /**
     * EXPORT từ Domain Model → Legacy System
     */
    public void syncToLegacy(Order order) {

        log.info("=== Syncing order to legacy system ===");
        log.info("Order ID: {}", order.getId());

        // STEP 1: Validate domain order
        if (!canSyncToLegacy(order)) {
            throw new IllegalStateException("Order cannot be synced to legacy");
        }

        // STEP 2: Translate domain → legacy format
        LegacyOrder legacyOrder = translateToLegacy(order);

        // STEP 3: Send to legacy system
        try {
            legacyClient.updateOrder(legacyOrder);
            log.info("=== Successfully synced to legacy system ===");
        } catch (RuntimeException e) {
            log.error("Failed to sync to legacy system", e);
            // Could retry, send to queue, or alert admin
            throw new RuntimeException("Sync failed", e);
        }
    }

    // ======== PRIVATE HELPER METHODS ========

    /**
     * Validate legacy data (Step 2)
     */
    private void validateLegacyData(LegacyOrder legacyOrder) {

        log.info("Validating legacy data");

        List<String> errors = new ArrayList<>();

        if (legacyOrder.getOrderNum() == null || legacyOrder.getOrderNum().isEmpty()) {
            errors.add("Order number is missing");
        }

        if (legacyOrder.getCustId() == null || legacyOrder.getCustId().isEmpty()) {
            errors.add("Customer ID is missing");
        }

        if (legacyOrder.getLineItems() == null || legacyOrder.getLineItems().isEmpty()) {
            errors.add("Order has no items");
        }

        // Validate line items
        if (legacyOrder.getLineItems() != null) {
            for (LegacyLineItem item : legacyOrder.getLineItems()) {
                if (item.getQty() <= 0) {
                    errors.add("Invalid quantity for product: " + item.getProdId());
                }
                if (item.getPrice() == null || item.getPrice().isEmpty()) {
                    errors.add("Missing price for product: " + item.getProdId());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Legacy data validation failed: " +
                String.join(", ", errors));
        }

        log.info("Legacy data validation passed");
    }

    /**
     * Translate legacy → domain (Step 3)
     */
    private Order translateToDomain(LegacyOrder legacyOrder) {

        log.info("Translating legacy order to domain model");

        // Translate order ID (add prefix)
        String domainOrderId = "ORD-" + legacyOrder.getOrderNum();

        // Translate status (numeric code → enum)
        OrderStatus status = translateStatus(
            legacyOrder.getStatusCode(),
            legacyOrder.getPaymentStatus()
        );

        // Translate date (string → LocalDateTime)
        LocalDateTime createdAt = parseDate(legacyOrder.getCreateDate());

        // Create domain order
        Order order = Order.builder()
            .id(domainOrderId)
            .customerId(legacyOrder.getCustId())
            .status(status)
            .items(new ArrayList<>())
            .createdAt(createdAt)
            .build();

        // Translate items
        for (LegacyLineItem legacyItem : legacyOrder.getLineItems()) {
            OrderItem domainItem = translateItem(legacyItem);
            order.getItems().add(domainItem);
        }

        // Calculate total
        order.setTotal(calculateTotal(order.getItems()));

        log.info("Translation completed");

        return order;
    }

    /**
     * Translate status with business rules
     */
    private OrderStatus translateStatus(int statusCode, String paymentStatus) {

        // Basic translation
        OrderStatus status = getOrderStatus(statusCode);

        // Apply business rule: Can't be CONFIRMED without payment
        if (status == OrderStatus.CONFIRMED && !"PAID".equals(paymentStatus)) {
            log.warn("Order marked as CONFIRMED but not paid, setting to PENDING");
            return OrderStatus.PENDING;
        }

        return status;
    }

    private static OrderStatus getOrderStatus(int statusCode) {
        switch (statusCode) {
            case 0:
                return OrderStatus.DRAFT;
            case 1:
                return OrderStatus.PENDING;
            case 2:
                return OrderStatus.CONFIRMED;
            case 3:
                return OrderStatus.SHIPPED;
            case 4:
                return OrderStatus.DELIVERED;
            case 9:
                return OrderStatus.CANCELLED;
            default:
                return OrderStatus.DRAFT;
        }
    }

    /**
     * Parse legacy date format
     */
    private LocalDateTime parseDate(String dateStr) {
        try {
            // Legacy format: "20240115143000" (yyyyMMddHHmmss)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}, using current time", dateStr);
            return LocalDateTime.now();
        }
    }

    /**
     * Translate line item
     */
    private OrderItem translateItem(LegacyLineItem legacyItem) {

        // Parse price (string → BigDecimal)
        BigDecimal price;
        try {
            price = new BigDecimal(legacyItem.getPrice());
        } catch (NumberFormatException e) {
            log.warn("Invalid price format: {}, using 0", legacyItem.getPrice());
            price = BigDecimal.ZERO;
        }

        return OrderItem.builder()
            .productId(legacyItem.getProdId())
            .productName(legacyItem.getProductName())
            .quantity(legacyItem.getQty())
            .price(price)
            .build();
    }

    /**
     * Enrich order with DB data (Step 4)
     */
    private void enrichOrder(Order order) {

        log.info("Enriching order with database data");

        // Enrich with customer data
        Customer customer = customerRepository.findById(order.getCustomerId())
            .orElseGet(() -> {
                log.warn("Customer not found in DB, creating placeholder");
                return createPlaceholderCustomer(order.getCustomerId());
            });

        // Enrich items with product data
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseGet(() -> {
                    log.warn("Product not found: {}", item.getProductId());
                    return createPlaceholderProduct(item);
                });

            // Update item with correct product info
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
        }

        log.info("Enrichment completed");
    }

    /**
     * Validate domain order (Step 5)
     */
    private void validateDomainOrder(Order order) {

        log.info("Validating domain order");

        if (order.getItems().isEmpty()) {
            throw new RuntimeException("Order must have items");
        }

        if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order total must be positive");
        }

        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new RuntimeException("Item quantity must be positive");
            }
            if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Item price cannot be negative");
            }
        }

        log.info("Domain validation passed");
    }

    /**
     * Translate domain → legacy (for export)
     */
    private LegacyOrder translateToLegacy(Order order) {

        log.info("Translating domain order to legacy format");

        LegacyOrder legacyOrder = new LegacyOrder();

        // Extract legacy ID (remove prefix)
        legacyOrder.setOrderNum(order.getId().replace("ORD-", ""));
        legacyOrder.setCustId(order.getCustomerId());

        // Translate status (enum → numeric)
        legacyOrder.setStatusCode(mapStatusToLegacy(order.getStatus()));
        legacyOrder.setPaymentStatus("PAID"); // Assume paid for confirmed orders

        // Format date (LocalDateTime → string)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        legacyOrder.setCreateDate(order.getCreatedAt().format(formatter));

        // Total as string
        legacyOrder.setTotalAmt(order.getTotal().toString());

        // Translate items
        List<LegacyLineItem> legacyItems = order.getItems().stream()
            .map(this::translateItemToLegacy)
            .collect(Collectors.toList());
        legacyOrder.setLineItems(legacyItems);

        return legacyOrder;
    }

    private int mapStatusToLegacy(OrderStatus status) {
        switch (status) {
            case DRAFT:
                return 0;
            case PENDING:
                return 1;
            case CONFIRMED:
                return 2;
            case SHIPPED:
                return 3;
            case DELIVERED:
                return 4;
            case CANCELLED:
                return 9;
        }
        return 0; // Default to DRAFT
    }

    private LegacyLineItem translateItemToLegacy(OrderItem item) {
        LegacyLineItem legacyItem = new LegacyLineItem();
        legacyItem.setProdId(item.getProductId());
        legacyItem.setProductName(item.getProductName());
        legacyItem.setQty(item.getQuantity());
        legacyItem.setPrice(item.getPrice().toString());
        return legacyItem;
    }

    // Helper methods

    private boolean canSyncToLegacy(Order order) {
        return order.getStatus() != OrderStatus.DRAFT;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Customer createPlaceholderCustomer(String customerId) {
        return Customer.builder()
            .id(customerId)
            .name("Legacy Customer")
            .email("legacy@example.com")
            .tier(CustomerTier.REGULAR)
            .build();
    }

    private Product createPlaceholderProduct(OrderItem item) {
        return Product.builder()
            .id(item.getProductId())
            .name(item.getProductName())
            .price(item.getPrice())
            .stock(0)
            .active(true)
            .build();
    }
}
