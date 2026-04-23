package org.app.common.support;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;

/**
 * Utility class for verifying arguments and throwing exceptions when verification fails.
 * Extends the Spring Framework's Assert class.
 */
public class Verify extends org.springframework.util.Assert {

    /**
     * Verifies that the actual value is less than the expected value.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the exception message if the verification fails
     */
    public static void lessThan(int expected, int actual, String message) {
        if (actual < expected) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that the actual value is greater than the expected value.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the exception message if the verification fails
     */
    public static void greaterThan(int expected, int actual, String message) {
        if (actual > expected) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that the actual value is less than or equal to the expected value.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the exception message if the verification fails
     */
    public static void lessThanOrEqual(int expected, int actual, String message) {
        if (actual <= expected) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that the actual value is greater than or equal to the expected value.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the exception message if the verification fails
     */
    public static void greaterThanOrEqual(int expected, int actual, String message) {
        if (actual >= expected) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that two objects are not equal.
     *
     * @param expected the expected object
     * @param actual   the actual object
     * @param message  the exception message if the verification fails
     */
    public static void ifNotEqual(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that two enum values are not equal.
     *
     * @param expected the expected enum value
     * @param actual   the actual enum value
     * @param message  the exception message if the verification fails
     * @param <E>      the type of the enum
     */
    public static <E extends Enum<E>> void ifNotEqual(E expected, E actual, String message) {
        if (expected != actual) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that two objects are equal.
     *
     * @param expected the expected object
     * @param actual   the actual object
     * @param message  the exception message if the verification fails
     */
    public static void ifEqual(Object expected, Object actual, String message) {
        if (expected == actual) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that two enum values are equal.
     *
     * @param expected the expected enum value
     * @param actual   the actual enum value
     * @param message  the exception message if the verification fails
     * @param <E>      the type of the enum
     */
    public static <E extends Enum<E>> void ifEqual(E expected, E actual, String message) {
        if (expected == actual) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a boolean expression is true.
     *
     * @param expression the boolean expression
     * @param message    the exception message if the verification fails
     */
    public static void ifTrue(boolean expression, String message) {
        if (expression) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a map contains a key with a value of true.
     *
     * @param map     the map to check
     * @param key     the key to look for
     * @param message the exception message if the verification fails
     */
    public static void ifValueTrue(Map<?, ?> map, String key, String message) {
        if (Boolean.TRUE.equals(map.get(key))) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a map contains a key with a value of false.
     *
     * @param map     the map to check
     * @param key     the key to look for
     * @param message the exception message if the verification fails
     */
    public static void ifValueFalse(Map<?, ?> map, String key, String message) {
        if (Boolean.FALSE.equals(map.get(key))) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that an object is null.
     *
     * @param object  the object to check
     * @param message the exception message if the verification fails
     */
    public static void ifNull(Object object, String message) {
        if (object == null) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that an object is not null.
     *
     * @param object  the object to check
     * @param message the exception message if the verification fails
     */
    public static void ifNotNull(Object object, String message) {
        if (object != null) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that an object is empty.
     *
     * @param object  the object to check
     * @param message the exception message if the verification fails
     */
    public static void isEmpty(Object object, String message) {
        if (ObjectUtils.isEmpty(object)) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a string is empty or contains only whitespace.
     *
     * @param str     the string to check
     * @param message the exception message if the verification fails
     */
    public static void ifEmpty(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a list is empty.
     *
     * @param list    the list to check
     * @param message the exception message if the verification fails
     */
    public static void ifEmpty(List<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a map is empty.
     *
     * @param map     the map to check
     * @param message the exception message if the verification fails
     */
    public static void ifEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new VerifyException(message);
        }
    }

    /**
     * Verifies that a string is not empty and contains text.
     *
     * @param str     the string to check
     * @param message the exception message if the verification fails
     */
    public static void ifHasText(String str, String message) {
        if (StringUtils.hasText(str)) {
            throw new VerifyException(message);
        }
    }

    /**
     * Require that value is within [min, max].
     */
    public static void requireBetween(int value, int minInclusive, int maxInclusive, String message) {
        if (value < minInclusive || value > maxInclusive)
            throw new VerifyException(message);
    }

    /**
     * Require map contains key.
     */
    public static void requireContainsKey(Map<?, ?> map, Object key, String message) {
        if (map == null || !map.containsKey(key))
            throw new VerifyException(message);
    }

    /**
     * Require that object is instance of given type.
     */
    public static void requireInstanceOf(Object obj, Class<?> type, String message) {
        if (!type.isInstance(obj)) throw new VerifyException(message);
    }

    /**
     * Require that subType is assignable to superType.
     */
    public static void requireAssignable(Class<?> superType, Class<?> subType, String message) {
        if (superType == null || subType == null || !superType.isAssignableFrom(subType)) {
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
