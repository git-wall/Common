package org.app.common.exception.infra;

import org.app.common.exception.base.AppException;
import org.app.common.exception.constant.ErrorType;

public class Http4xxException extends AppException {
    private static final long serialVersionUID = 5261336629160232392L;

    public Http4xxException(String ctx, Throwable c) {
        super(ErrorType.HTTP_4XX, ctx, "HTTP 4xx", c);
    }

    public Http4xxException(String ctx, String m) {
        super(ErrorType.NOT_FOUND, ctx, m, null);
    }

    public static Http4xxException buildInvalidRequest(String ctx) {
        var m = String.format("%s invalid request", ctx);
        return new Http4xxException(ctx, m);
    }
}
