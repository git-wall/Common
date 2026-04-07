package org.app.core.exception.infra;

import org.app.core.exception.base.AppException;
import org.app.core.exception.constant.ErrorType;

public class Http5xxException extends AppException {
    private static final long serialVersionUID = 8556297129800048701L;

    public Http5xxException(String ctx, Throwable c) {
        super(ErrorType.HTTP_5XX, ctx, "HTTP 5xx", c);
    }
}
