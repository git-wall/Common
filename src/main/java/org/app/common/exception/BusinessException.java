package org.app.common.exception;

public class BusinessException extends Exception {
    private static final long serialVersionUID = -1595757622964685690L;
    public BusinessException(String message) {
        super(message);
    }
}
