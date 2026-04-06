package org.app.core.support;

import lombok.NoArgsConstructor;
import org.app.core.exception.logic.VerifyException;
import org.app.core.utils.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for verifying arguments and throwing exceptions when verification fails.
 * Extends the Spring Framework's Assert class.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public abstract class Verify {

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

    public static <T extends Throwable> void ifEqual(Object expected, Object actual, Supplier<T> supplierThrowable) throws T {
        if (expected == actual) {
            throw supplierThrowable.get();
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

    public static <T extends Throwable> void ifTrue(boolean expression, Supplier<T> supplierThrowable) throws T {
        if (expression) {
            throw supplierThrowable.get();
        }
    }

    public static void ifFalse(boolean expression, String message) {
        if (!expression) {
            throw new VerifyException(message);
        }
    }

    public static <T extends Throwable> void ifFalse(boolean expression, Supplier<T> supplierThrowable) throws T {
        if (!expression) {
            throw supplierThrowable.get();
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

    public static <T extends Throwable> void ifNull(Object object, Supplier<T> supplierThrowable) throws T {
        if (object == null) {
            throw supplierThrowable.get();
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

    public static void dataNonNull(Object data) {
        if (data == null) {
            throw new VerifyException("Data cannot be null");
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

    public static <T extends Throwable> void ifEmpty(String str, Supplier<T> supplierThrowable) throws T {
        if (!StringUtils.hasText(str)) {
            throw supplierThrowable.get();
        }
    }

    /**
     * Verifies that a list is empty.
     *
     * @param list    the list to check
     * @param message the exception message if the verification fails
     */
    public static void ifEmpty(Collection<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new VerifyException(message);
        }
    }

    public static <T extends Throwable> void ifEmpty(Collection<?> list, Supplier<T> supplierThrowable) throws T {
        if (list == null || list.isEmpty()) {
            throw supplierThrowable.get();
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

    public static <T extends Throwable> void ifEmpty(Map<?, ?> map, Supplier<T> supplierThrowable) throws T {
        if (map == null || map.isEmpty()) {
            throw supplierThrowable.get();
        }
    }

    public static void ifEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new VerifyException(message);
        }
    }

    public static <T extends Throwable> void ifEmpty(Object[] array, Supplier<T> supplierThrowable) throws T {
        if (array == null || array.length == 0) {
            throw supplierThrowable.get();
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

    public static <T extends Throwable> void ifHasText(String str, Supplier<T> supplierThrowable) throws T {
        if (StringUtils.hasText(str)) {
            throw supplierThrowable.get();
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

