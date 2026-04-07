package org.app.common.design.platform.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.design.platform.infrastructure.event.CommandCompletedEvent;
import org.app.common.design.platform.infrastructure.event.CommandDispatchedEvent;
import org.app.common.design.platform.infrastructure.event.CommandFailedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandBus {

    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;
    private final ApplicationEventPublisher eventPublisher;

    @SuppressWarnings("unchecked")
    public <R> R dispatch(Command command) {

        log.info("Dispatching command: {}", command.getClass().getSimpleName());

        CommandHandler<Command, R> handler = (CommandHandler<Command, R>)
            handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler for command: " + command.getClass()
            );
        }

        // Pre-execution event
        eventPublisher.publishEvent(new CommandDispatchedEvent(command));

        try {
            R result = handler.handle(command);

            // Post-execution event
            eventPublisher.publishEvent(new CommandCompletedEvent(command, result));

            return result;
        } catch (Exception e) {
            eventPublisher.publishEvent(new CommandFailedEvent(command, e));
            throw e;
        }
    }
}
