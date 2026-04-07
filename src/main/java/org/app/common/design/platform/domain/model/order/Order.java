package org.app.common.design.platform.domain.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.common.support.Verify;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    private String id;

    @Column(name = "customer_id")
    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ========== AGGREGATE ROOT - Business Methods ==========

    public void addItem(OrderItem item) {
        Verify.lessThanOrEqual(item.getQuantity(), 0, "Quantity must be positive");
        Verify.ifNotEqual(status, OrderStatus.DRAFT, "Cannot modify confirmed order");
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    public void confirm() {
        Verify.ifNotEqual(status, OrderStatus.PENDING, "Only pending orders can be confirmed");
        Verify.ifEmpty(items, "Order items cannot be empty");
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        Verify.ifNotEqual(status, OrderStatus.CONFIRMED, "Only confirmed orders can be shipped");
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        Verify.ifNotEqual(status, OrderStatus.SHIPPED, "Only shipped orders can be delivered");
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        Verify.ifEqual(status, OrderStatus.DELIVERED, "Cannot cancel delivered order");
        this.status = OrderStatus.CANCELLED;
    }

    public boolean canCancel() {
        return status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED;
    }

    public boolean canReturn() {
        return status == OrderStatus.DELIVERED
            && createdAt.plusDays(30).isAfter(LocalDateTime.now());
    }

    private void recalculateTotal() {
        this.total = items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
