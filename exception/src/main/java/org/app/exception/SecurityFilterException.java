package org.app.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@StandardException
public class SecurityFilterException extends RuntimeException {
    private static final long serialVersionUID = 1625248970837825410L;
    @Setter
    @Getter
    private String code;
}
