package org.app.core.exception;

import lombok.Getter;

public class ServerException extends RuntimeException {
    private static final long serialVersionUID = -8955522250007111931L;
    @Getter
    private static final String MV = "Lỗi hệ thống";
    @Getter
    private static final String ME = "Server error";

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
