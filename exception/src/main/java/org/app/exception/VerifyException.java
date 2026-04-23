package org.app.exception;

public class VerifyException extends SlightException {
    private static final long serialVersionUID = 2303591199671755706L;

    static {
        StackLocators.registerIgnoreClass(VerifyException.class);
    }

    public VerifyException(String message) {
        super(message);
    }
}
