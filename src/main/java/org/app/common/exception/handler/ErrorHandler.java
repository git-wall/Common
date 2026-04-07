package org.app.common.exception.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.ConextKey;
import org.app.common.exception.base.AppException;
import org.app.common.exception.policy.ErrorPolicy;
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
                log.warn("{} Unhandled error", MDC.get(ConextKey.REQUEST_ID), e);
                throw e;

            default:
                throw e;
        }
    }
}
