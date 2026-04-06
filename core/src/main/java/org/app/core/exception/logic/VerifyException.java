package org.app.core.exception.logic;

public class VerifyException extends RuntimeException {
    private static final long serialVersionUID = -2763295777377242106L;

    public VerifyException(String message) {
        super(message);
    }

    public VerifyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, null, false, false);
        //'writableStackTrace' = false will off about collect stacktrace
    }

    public static VerifyException of(String message) {
        return new VerifyException(message);
    }

    public static VerifyException withOutTrace(String message) {
        return new VerifyException(message, null, false, false);
    }

    // skip trace for free cpu to bring message to ouside
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // do nothing
    }
}
