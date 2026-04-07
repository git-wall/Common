package org.app.common.exception.base;

import org.app.common.exception.constant.ErrorType;


public abstract class AppException extends RuntimeException {
    private static final long serialVersionUID = -7359808740744295161L;
    private final ErrorType type;
    private final String context;

    protected AppException(
        ErrorType type,
        String context,
        String message,
        Throwable cause
    ) {
        super(message, cause);
        this.type = type;
        this.context = context;
    }

    public ErrorType type() {
        return type;
    }

    public String context() {
        return context;
    }
}
