package org.app.core.exception.business;

import org.app.core.exception.base.AppException;
import org.app.core.exception.constant.ErrorType;

public class BusinessException extends AppException {
    private static final long serialVersionUID = -1595757622964685690L;

    public static final String ERROR = "business error";

    public BusinessException(String ctx, String msg) {
        super(ErrorType.BUSINESS, ctx, msg, null);
    }

    public static BusinessException build(String ctx, String msg) {
        var message = String.format("%s : %s : %s", ctx, ERROR, msg);
        return new BusinessException(ctx, message);
    }

    public BusinessException(String msg) {
        super(msg, null, false, false);
        //'writableStackTrace' = false will off about collect stacktrace
    }

    // skip trace for free cpu to bring message to outside
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // do nothing
    }
}
