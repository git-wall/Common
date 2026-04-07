package org.app.common.design.platform.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// Event Publisher - wrapper around Spring
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(OrderEvent event) {
        log.info("Publishing event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
