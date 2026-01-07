package org.app.common.support;

import org.springframework.util.StringUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;
import org.thymeleaf.util.ListUtils;

import java.util.List;
import java.util.Map;


// verify arguments, throw IllegalArgumentException when verification fails
public class Verify extends org.springframework.util.Assert {

    public static void lessThan(int expected, int actual, String message) {
        if (actual < expected) {
            throw new VerifyException(message);
        }
    }

    public static void greaterThan(int expected, int actual, String message) {
        if (actual > expected) {
            throw new VerifyException(message);
        }
    }

    public static void lessThanOrEqual(int expected, int actual, String message) {
        if (actual <= expected) {
            throw new VerifyException(message);
        }
    }

    public static void greaterThanOrEqual(int expected, int actual, String message) {
        if (actual >= expected) {
            throw new VerifyException(message);
        }
    }

    public static void ifNotEqual(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new VerifyException(message);
        }
    }

    public static <E extends Enum<E>> void ifNotEqual(E expected, E actual, String message) {
        if (expected != actual) {
            throw new VerifyException(message);
        }
    }

    public static void ifEqual(Object expected, Object actual, String message) {
        if (expected == actual) {
            throw new VerifyException(message);
        }
    }

    public static <E extends Enum<E>> void ifEqual(E expected, E actual, String message) {
        if (expected == actual) {
            throw new VerifyException(message);
        }
    }

    public static void ifTrue(boolean expression, String message) {
        if (expression) {
            throw new VerifyException(message);
        }
    }

    public static void ifValueTrue(Map<?, ?> map, String key, String message) {
        if (Boolean.TRUE.equals(map.get(key))) {
            throw new VerifyException(message);
        }
    }

    public static void ifValueFalse(Map<?, ?> map, String key, String message) {
        if (Boolean.FALSE.equals(map.get(key))) {
            throw new VerifyException(message);
        }
    }

    public static void ifNull(Object object, String message) {
        if (object == null) {
            throw new VerifyException(message);
        }
    }

    public static void ifNotNull(Object object, String message) {
        if (object != null) {
            throw new VerifyException(message);
        }
    }

    public static void isEmpty(Object object, String message) {
        if (ObjectUtils.isEmpty(object)) {
            throw new VerifyException(message);
        }
    }

    public static void ifEmpty(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new VerifyException(message);
        }
    }

    public static void ifEmpty(List<?> list, String message) {
        if (ListUtils.isEmpty(list)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void ifEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new VerifyException(message);
        }
    }

    public static void ifHasText(String str, String message) {
        if (StringUtils.hasText(str)) {
            throw new VerifyException(message);
        }
    }
}

class VerifyException extends RuntimeException {
    private static final long serialVersionUID = -2763295777377242106L;

    public VerifyException(String message) {
        super(message);
    }
}
