package org.app.common.design.platform.infrastructure.event;

import lombok.Value;
import org.app.common.design.platform.infrastructure.messaging.Command;

import java.time.LocalDateTime;

@Value
public class CommandDispatchedEvent {
    Command command;
    LocalDateTime dispatchedAt;

    public CommandDispatchedEvent(Command command) {
        this.command = command;
        this.dispatchedAt = LocalDateTime.now();
    }
}
