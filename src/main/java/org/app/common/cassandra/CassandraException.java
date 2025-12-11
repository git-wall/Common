package org.app.common.cassandra;

public class CassandraException extends RuntimeException {
    private static final long serialVersionUID = -2139561247799478541L;
    private final String errorCode;

    public CassandraException(String message) {
        super(message);
        this.errorCode = "CASSANDRA_ERROR";
    }

    public CassandraException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CASSANDRA_ERROR";
    }

    public CassandraException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CassandraException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
