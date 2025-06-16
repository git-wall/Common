package org.app.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.springframework.util.Assert;

@Slf4j
public class MessageUtils {

    public static void log(Object message) {
        Assert.notNull(message, "Message must not be null");
        if (message instanceof Throwable) {
            log.error("Error occurred: {}", JacksonUtils.toString(message));
        } else {
            log.info("Message: {}", message);
        }
        System.out.println("Message: " + message);
    }

    public static void logWithTracing(Object message) {
        Assert.notNull(message, "Message must not be null");
        if (message instanceof Throwable) {
            log.error("{} - Error occurred: {}", TracingContext.getRequestId(), JacksonUtils.toString(message));
        } else {
            log.info("{} - Message: {}", TracingContext.getRequestId(), message);
        }
        System.out.println("Message: " + message);
    }
}
