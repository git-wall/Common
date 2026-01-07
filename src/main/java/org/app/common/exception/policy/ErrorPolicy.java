package org.app.common.exception.policy;

import org.app.common.exception.base.AppException;
import org.app.common.exception.constant.ErrorAction;
import org.app.common.exception.constant.ErrorType;

import java.util.Map;

public class ErrorPolicy {

    private static final Map<ErrorType, ErrorAction> MAP;

    static {
        MAP = Map.of(
                ErrorType.NOT_FOUND, ErrorAction.RETURN_EMPTY,
                ErrorType.UNKNOWN, ErrorAction.LOG_AND_THROW
        );
    }

    public static ErrorAction actionFor(AppException e) {
        return MAP.getOrDefault(e.type(), ErrorAction.THROW);
    }
}
