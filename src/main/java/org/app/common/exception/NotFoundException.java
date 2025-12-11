package org.app.common.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@Setter
@Getter
@StandardException
public class NotFoundException extends RuntimeException {
}
