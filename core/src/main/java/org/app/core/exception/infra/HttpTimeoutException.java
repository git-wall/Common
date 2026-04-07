package org.app.core.exception.infra;

import org.app.core.exception.base.AppException;
import org.app.core.exception.constant.ErrorType;

public class HttpTimeoutException extends AppException {
    private static final long serialVersionUID = -977349584537184587L;

    public HttpTimeoutException(String ctx, Throwable c) {
        super(ErrorType.TIMEOUT, ctx, "HTTP timeout", c);
    }
}
