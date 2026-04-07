package org.app.core.exception.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.core.exception.base.AppException;
import org.app.core.exception.policy.ErrorPolicy;
import org.slf4j.MDC;

import java.util.function.Supplier;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorHandler {

    public static <T> T handle(AppException e, Supplier<T> emptySupplier) {
        switch (ErrorPolicy.actionFor(e)) {
            case RETURN_EMPTY:
                return emptySupplier.get();

            case LOG_AND_THROW:
                log.warn("{} Unhandled error", MDC.get("X-Request-Id"), e);
                throw e;

            default:
                throw e;
        }
    }
}
