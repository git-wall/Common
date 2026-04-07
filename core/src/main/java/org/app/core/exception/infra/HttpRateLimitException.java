package org.app.core.exception.infra;

import org.app.core.exception.base.AppException;
import org.app.core.exception.constant.ErrorType;

public class HttpRateLimitException extends AppException {
    private static final long serialVersionUID = 842261899803297222L;

    public HttpRateLimitException(String ctx, Throwable c) {
        super(ErrorType.RATE_LIMIT, ctx, "Rate limited", c);
    }
}
