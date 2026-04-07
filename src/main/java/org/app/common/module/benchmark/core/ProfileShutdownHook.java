package org.app.common.module.benchmark.core;


import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.aggregate.ProfileAggregator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Slf4j
public class ProfileShutdownHook implements ApplicationListener<ContextClosedEvent> {

    private static volatile boolean flushed = false;

    public ProfileShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!flushed) {
                log.info("JVM shutdown - flushing profiles...");
                flushProfiles();
            }
        }, "profile-shutdown-hook"));
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Spring context closing - flushing profiles...");
        flushProfiles();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("@PreDestroy - flushing profiles...");
        flushProfiles();
    }

    private synchronized void flushProfiles() {
        if (flushed) {
            return;
        }

        try {
            ProfileAggregator.flush();
            flushed = true;
            log.info("✅ Profile data flushed - created new version");

        } catch (Exception e) {
            log.error("❌ Failed to flush profile data", e);
        }
    }
}
