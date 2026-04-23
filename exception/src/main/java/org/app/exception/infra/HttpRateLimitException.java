package org.app.exception.infra;

import org.app.exception.base.AppException;
import org.app.exception.constant.ErrorType;

public class HttpRateLimitException extends AppException {
    private static final long serialVersionUID = 842261899803297222L;

    public HttpRateLimitException(String ctx, Throwable c) {
        super(ErrorType.RATE_LIMIT, ctx, "Rate limited", c);
    }
}
