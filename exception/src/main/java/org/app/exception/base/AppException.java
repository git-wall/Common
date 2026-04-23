package org.app.exception.base;


import org.app.exception.constant.ErrorType;

public abstract class AppException extends RuntimeException {
    private static final long serialVersionUID = -7359808740744295161L;
    private ErrorType type;
    private String context;

    public ErrorType type() {
        return type;
    }

    public String context() {
        return context;
    }

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

    protected AppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
