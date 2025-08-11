package org.app.common.exception;

import lombok.experimental.StandardException;

@StandardException
public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = -1595757622964685690L;
}
