package org.app.common.design.platform.application.usecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public abstract class BaseUseCase<REQUEST, RESPONSE> {

    protected final ApplicationEventPublisher eventPublisher;

    protected BaseUseCase(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public RESPONSE execute(REQUEST request) {
        log.info("Executing use case: {}", this.getClass().getSimpleName());
        validate(request);
        RESPONSE response = process(request);
        publishEvents(request, response);
        return response;
    }

    protected abstract void validate(REQUEST request);
    protected abstract RESPONSE process(REQUEST request);
    protected abstract void publishEvents(REQUEST request, RESPONSE response);
}
