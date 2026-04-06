package org.app.common.exception;

import lombok.Getter;

public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 3903362851186556303L;
    @Getter
    private final String messageV = "Lỗi dịch vụ";
    @Getter
    private final String messageE = "Service error";

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
