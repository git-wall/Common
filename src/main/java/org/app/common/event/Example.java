package org.app.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;

public class Example {

    public class OrderEvent extends ApplicationEvent {

        public OrderEvent(Object source) {
            super(source);
        }

        public OrderEvent(Object source, Clock clock) {
            super(source, clock);
        }
    }

    @Service
    public class OrderService {
        @Autowired
        private org.springframework.context.ApplicationEventPublisher eventPublisher;

        @Transactional
        public void placeOrder(Object order) {
            // Business logic for placing an order
            // ...

            // Publish an OrderEvent
            OrderEvent orderEvent = new OrderEvent(order);
            eventPublisher.publishEvent(orderEvent);
        }
    }

    @Component
    public class OrderEmailListener {
        @Async
        @org.springframework.context.event.EventListener
        public void handleOrderEvent(OrderEvent event) {
            // Logic to send email notification
            System.out.println("Sending email notification for order: " + event.getSource());
        }
    }

    public class OrderLoggingListener {
        // sau khi transaction commit thi
        @Async
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handleOrderEvent(OrderEvent event) {
            // Logic to log order details
            System.out.println("Logging order details: " + event.getSource());
        }
    }
}
