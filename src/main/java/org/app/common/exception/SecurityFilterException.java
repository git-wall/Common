package org.app.common.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@Setter
@Getter
@StandardException
public class SecurityFilterException extends RuntimeException {
    private static final long serialVersionUID = 1625248970837825410L;
    private String code;
}