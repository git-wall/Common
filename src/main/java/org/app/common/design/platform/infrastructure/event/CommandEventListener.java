package org.app.common.design.platform.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandEventListener {

    @EventListener
    public void onCommandDispatched(CommandDispatchedEvent event) {
        log.info("📊 Command dispatched: {} at {}",
            event.getCommand().getClass().getSimpleName(),
            event.getDispatchedAt()
        );

        // Could: Send to monitoring system (Prometheus, DataDog)
        // Could: Store in audit log
    }

    @EventListener
    public void onCommandCompleted(CommandCompletedEvent event) {
        log.info("📊 Command completed: {} at {}",
            event.getCommand().getClass().getSimpleName(),
            event.getCompletedAt()
        );

        // Could: Track metrics (execution time, success rate)
        // Could: Update dashboard
    }

    @EventListener
    public void onCommandFailed(CommandFailedEvent event) {
        log.error("📊 Command failed: {} - Error: {}",
            event.getCommand().getClass().getSimpleName(),
            event.getException().getMessage()
        );

        // Could: Send alert to Slack/PagerDuty
        // Could: Store error in error tracking system (Sentry)
        // Could: Retry failed command
    }
}
