package org.app.core.exception.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.core.exception.base.AppException;
import org.app.core.exception.constant.ErrorAction;
import org.app.core.exception.constant.ErrorType;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
