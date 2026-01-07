package org.app.common.design.platform.infrastructure.messaging;

public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}
