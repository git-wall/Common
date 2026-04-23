package org.app.exception.constant;

import java.util.Arrays;

public enum ErrorType {
    INVALID_REQUEST,
    NOT_FOUND,
    IO,
    TIMEOUT,
    NETWORK,
    RATE_LIMIT,
    HTTP_4XX,
    HTTP_5XX,
    BUSINESS,
    VALIDATION,
    SERIALIZATION,
    RESOURCE_EXHAUSTED,
    UNKNOWN;

    public static ErrorType fromString(String type) {
        return Arrays.stream(values())
            .filter(e -> e.name().equals(type))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
