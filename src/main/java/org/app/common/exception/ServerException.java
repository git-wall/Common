package org.app.common.exception;

import lombok.Getter;

public class ServerException extends RuntimeException {
    private static final long serialVersionUID = -8955522250007111931L;
    @Getter
    private final String messageV = "Lỗi hệ thống";
    @Getter
    private final String messageE = "Server error";
    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
