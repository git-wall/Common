package org.app.common.design.platform.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent: {}", event.getOrderId());
        // notify inventory service to reserve stock
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Handling OrderConfirmedEvent: {}", event.getOrderId());
        // comfirm stock reservation
        // notify payment service to process payment
    }

    @EventListener
    @Async
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Handling OrderShippedEvent: {}", event.getOrderId());
        // notify customer with tracking info
    }

    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent: {}", event.getOrderId());
        // release stock reservation
        // notify customer of cancellation
    }
}
