package org.app.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.app.common.exception.base.AppException;
import org.app.common.exception.policy.ErrorPolicy;

import java.util.function.Supplier;

@Slf4j
public class ErrorHandler {

    public static <T> T handle(AppException e, Supplier<T> emptySupplier) {
        switch (ErrorPolicy.actionFor(e)) {
            case RETURN_EMPTY:
                return emptySupplier.get();

            case LOG_AND_THROW:
                log.warn("{} Unhandled error", TracingContext.getRequestId(), e);
                throw e;

            default:
                throw e;
        }
    }
}
