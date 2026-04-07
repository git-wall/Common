package org.app.core.exception.logic;

import lombok.Getter;

public class LogicException extends RuntimeException {
    @Getter
    private final String messageDetail;
    @Getter
    private final transient Object data;

    private static final long serialVersionUID = -5238354372804965985L;

    public LogicException(String message, String messageDetail, Object data) {
        super(message);
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public LogicException(String message,
                          String messageDetail,
                          Object data,
                          Throwable cause,
                          boolean enableSuppression,
                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.messageDetail = messageDetail;
        this.data = data;
        //'writableStackTrace' = false will off about collect stacktrace
    }

    public static LogicException of(String message) {
        return new LogicException(message, null, null);
    }

    public static LogicException of(String message, String detail) {
        return new LogicException(message, detail, null);
    }

    public static LogicException of(String message, String detail, Object data) {
        return new LogicException(message, detail, data);
    }

    public static LogicException withoutTrace(String message) {
        return new LogicException(message, null, null, null, false, false);
    }

    public static LogicException withoutTrace(String message, String detail) {
        return new LogicException(message, detail, null, null, false, false);
    }

    public static LogicException withoutTrace(String message, String detail, Object data) {
        return new LogicException(message, detail, data, null, false, false);
    }

    // skip trace for free cpu to bring message to outside
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // do nothing
    }
}
