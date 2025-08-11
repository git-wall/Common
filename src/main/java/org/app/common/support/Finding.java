package org.app.common.support;

import org.apache.http.config.Lookup;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Utility class providing methods to create and work with Lookup objects.
 * A Lookup is a functional interface that maps a String key to a value of type T.
 */
public class Finding {

    public Finding() {
    }

    /**
     * Creates a Lookup from a function that maps String to T.
     *
     * @param function the function to convert to a Lookup
     * @param <T> the type of values returned by the Lookup
     * @return a Lookup that delegates to the provided function
     */
    public static <T> Lookup<T> lookup(Function<String, T> function) {
        return function::apply;
    }

    /**
     * Creates a Lookup that finds fields in a class by name.
     *
     * @param clazz the class to look up fields in
     * @return a Lookup that returns Field objects
     */
    public static Lookup<Field> lookupField(Class<?> clazz) {
        return n -> {
            try {
                return clazz.getDeclaredField(n);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Field not found: " + n, e);
            }
        };
    }

    /**
     * Creates a Lookup that finds methods in a class by name.
     *
     * @param clazz the class to look up methods in
     * @return a Lookup that returns Method objects
     */
    public static Lookup<Method[]> lookupMethod(Class<?> clazz) {
        return n -> {
            Method[] methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.getName().equals(n))
                    .toArray(Method[]::new);

            if (methods.length == 0) {
                throw new IllegalArgumentException("Method not found: " + n);
            }
            return methods;
        };
    }

    /**
     * Creates a Lookup that finds resources using a ClassLoader.
     *
     * @param classLoader the ClassLoader to use for resource lookup
     * @return a Lookup that returns InputStream objects for resources
     */
    public static Lookup<InputStream> lookupResource(ClassLoader classLoader) {
        return n -> {
            InputStream is = classLoader.getResourceAsStream(n);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + n);
            }
            return is;
        };
    }

    /**
     * Creates a Lookup that finds values in a Properties object.
     *
     * @param properties the Properties object to look up values in
     * @return a Lookup that returns String values from the Properties
     */
    public static Lookup<String> lookupProperty(Properties properties) {
        return n -> {
            String value = properties.getProperty(n);
            if (value == null) {
                throw new IllegalArgumentException("Property not found: " + n);
            }
            return value;
        };
    }

    /**
     * Creates a Lookup that finds values in a Map.
     *
     * @param map the Map to look up values in
     * @param <T> the type of values in the Map
     * @return a Lookup that returns values from the Map
     */
    public static <T> Lookup<T> lookupMap(Map<String, T> map) {
        return n -> {
            T value = map.get(n);
            if (value == null) {
                throw new IllegalArgumentException("Key not found in map: " + n);
            }
            return value;
        };
    }

    /**
     * Creates a Lookup that finds environment variables.
     *
     * @return a Lookup that returns environment variable values
     */
    public static Lookup<String> lookupEnvironment() {
        return n -> {
            String value = System.getenv(n);
            if (value == null) {
                throw new IllegalArgumentException("Environment variable not found: " + n);
            }
            return value;
        };
    }

    /**
     * Creates a Lookup that finds system properties.
     *
     * @return a Lookup that returns system property values
     */
    public static Lookup<String> lookupSystemProperty() {
        return n -> {
            String value = System.getProperty(n);
            if (value == null) {
                throw new IllegalArgumentException("System property not found: " + n);
            }
            return value;
        };
    }

    /**
     * Creates a Lookup that returns a default value if the key is not found.
     *
     * @param lookup the primary Lookup to use
     * @param defaultValue the default value to return if the key is not found
     * @param <T> the type of values returned by the Lookup
     * @return a Lookup with default value handling
     */
    public static <T> Lookup<T> withDefault(Lookup<T> lookup, T defaultValue) {
        return n -> {
            try {
                return lookup.lookup(n);
            } catch (Exception e) {
                return defaultValue;
            }
        };
    }

    /**
     * Creates a Lookup that transforms the values returned by another Lookup.
     *
     * @param lookup the source Lookup
     * @param transformer the function to transform values
     * @param <T> the input type
     * @param <R> the output type
     * @return a transforming Lookup
     */
    public static <T, R> Lookup<R> transform(Lookup<T> lookup, Function<T, R> transformer) {
        return n -> {
            T value = lookup.lookup(n);
            return transformer.apply(value);
        };
    }
}
