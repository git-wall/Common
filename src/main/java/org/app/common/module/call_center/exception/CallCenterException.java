package org.app.common.module.call_center.exception;

public class CallCenterException extends RuntimeException {
    private static final long serialVersionUID = -3590848684999076088L;

    public CallCenterException(String message) {
        super(message);
    }

    public CallCenterException(String message, Throwable cause) {
        super(message, cause);
    }
}
