package org.app.common.design.platform.infrastructure.event;

import lombok.Value;
import org.app.common.design.platform.infrastructure.messaging.Command;

import java.time.LocalDateTime;

@Value
public class CommandFailedEvent {
    Command command;
    Exception exception;
    LocalDateTime failedAt;

    public CommandFailedEvent(Command command, Exception exception) {
        this.command = command;
        this.exception = exception;
        this.failedAt = LocalDateTime.now();
    }
}
