package org.app.common.scylla;

public class ScyllaException extends RuntimeException {
    public ScyllaException(String message) {
        super(message);
    }

    public ScyllaException(String message, Throwable cause) {
        super(message, cause);
    }
}