package org.app.common.exception;

public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = -1595757622964685690L;
    public TooManyRequestsException(String message) {
        super(message);
    }
}
