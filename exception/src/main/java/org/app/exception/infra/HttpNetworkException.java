package org.app.exception.infra;

import org.app.exception.base.AppException;
import org.app.exception.constant.ErrorType;

public class HttpNetworkException extends AppException {
    private static final long serialVersionUID = -4869770124173351595L;

    public HttpNetworkException(String ctx, Throwable c) {
        super(ErrorType.NETWORK, ctx, "HTTP network error", c);
    }
}
