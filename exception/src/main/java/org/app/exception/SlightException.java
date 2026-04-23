package org.app.exception;

import lombok.Getter;

public class SlightException extends RuntimeException {
    private static final long serialVersionUID = -4437490184798614741L;
    @Getter
    private final String location;

    protected SlightException(String message) {
        super(message, null, false, false);
        this.location = StackLocators.findLocation(3);
    }

    protected SlightException(String message, Throwable cause) {
        super(message, cause, false, false);
        this.location = StackLocators.findLocation(3);
    }

    protected SlightException(String message, int limit) {
        super(message, null, false, false);
        this.location = StackLocators.findLocation(limit);
    }

    protected SlightException(String message, Throwable cause, int limit) {
        super(message, cause, false, false);
        this.location = StackLocators.findLocation(limit);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    static {
        StackLocators.registerIgnoreClass(SlightException.class);
    }
}

