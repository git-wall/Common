package org.app.core.exception;

import lombok.Getter;

@Getter
public class ResponseException extends RuntimeException {
    private static final long serialVersionUID = -6931364898754208319L;

    private final String messageDetail;
    private final transient Object data;


    public ResponseException(String message, String messageDetail, Object data) {
        super(message);
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public ResponseException(String message, Throwable cause, String messageDetail, Object data) {
        super(message, cause);
        this.messageDetail = messageDetail;
        this.data = data;
    }

    public ResponseException(Throwable cause, String messageDetail, Object data) {
        super(cause);
        this.messageDetail = messageDetail;
        this.data = data;
    }

    // skip trace for free cpu to bring message to outside
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // do nothing
    }
}
