package org.app.exception.infra;

import org.app.exception.base.AppException;
import org.app.exception.constant.ErrorType;

public class HttpTimeoutException extends AppException {
    private static final long serialVersionUID = -977349584537184587L;

    public HttpTimeoutException(String ctx, Throwable c) {
        super(ErrorType.TIMEOUT, ctx, "HTTP timeout", c);
    }
}
