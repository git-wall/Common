package org.app.common.design.platform.repo;

import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId " +
        "AND o.status = :status " +
        "AND o.createdAt BETWEEN :fromDate AND :toDate")
    List<Order> findByCustomerAndStatusAndDateRange(
        @Param("customerId") String customerId,
        @Param("status") OrderStatus status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    @Query(value = "SELECT * FROM orders WHERE total > :minTotal " +
        "ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true)
    List<Order> findHighValueOrders(
        @Param("minTotal") BigDecimal minTotal,
        @Param("limit") int limit
    );

    boolean existsByCustomerIdAndStatus(String customerId, OrderStatus status);
}
