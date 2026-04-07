package org.app.core.exception;

import lombok.Getter;

public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 3903362851186556303L;

    @Getter
    private static final String MV = "Lỗi dịch vụ";
    @Getter
    private static final String ME = "Service error";

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
