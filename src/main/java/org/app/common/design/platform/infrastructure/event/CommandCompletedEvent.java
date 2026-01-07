package org.app.common.design.platform.infrastructure.event;

import lombok.Value;
import org.app.common.design.platform.infrastructure.messaging.Command;

import java.time.LocalDateTime;

@Value
public class CommandCompletedEvent {
    Command command;
    Object result;
    LocalDateTime completedAt;
    long executionTimeMs;

    public CommandCompletedEvent(Command command, Object result) {
        this.command = command;
        this.result = result;
        this.completedAt = LocalDateTime.now();
        this.executionTimeMs = 0; // Calculate if needed
    }
}
