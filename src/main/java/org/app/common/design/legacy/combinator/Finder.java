package org.app.common.design.legacy.combinator;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Optional;

/**
 * A generic Finder interface that uses the combinator pattern to compose functions.
 * This interface provides methods to combine finders using logical operations (and, or, not)
 * and to apply transformations to the results.
 *
 * @param <T> the type of object to find
 */
public interface Finder<T> extends Function<T, Boolean> {

    /**
     * Applies this finder to the given input.
     *
     * @param t the input object
     * @return true if the object matches the finder criteria, false otherwise
     */
    @Override
    default Boolean apply(T t) {
        return find(t);
    }

    /**
     * Determines if the given object matches the finder criteria.
     *
     * @param t the object to check
     * @return true if the object matches the finder criteria, false otherwise
     */
    boolean find(T t);

    /**
     * Returns a finder that is the logical AND of this finder and the other finder.
     *
     * @param other the other finder
     * @return a new finder that is the logical AND of this finder and the other finder
     */
    default Finder<T> and(Finder<T> other) {
        return t -> this.find(t) && other.find(t);
    }

    /**
     * Returns a finder that is the logical OR of this finder and the other finder.
     *
     * @param other the other finder
     * @return a new finder that is the logical OR of this finder and the other finder
     */
    default Finder<T> or(Finder<T> other) {
        return t -> this.find(t) || other.find(t);
    }

    /**
     * Returns a finder that is the logical negation of this finder.
     *
     * @return a new finder that is the logical negation of this finder
     */
    default Finder<T> not() {
        return t -> !this.find(t);
    }

    /**
     * Returns a finder that applies a transformation to the input before applying this finder.
     *
     * @param <R> the type of the input to the transformation
     * @param before the transformation to apply
     * @return a new finder that applies the transformation before applying this finder
     */
    default <R> Finder<R> composeTransform(Function<R, T> before) {
        return r -> this.find(before.apply(r));
    }

    /**
     * Returns a finder that applies this finder and then applies a transformation to the result.
     *
     * @param <R> the type of the output of the transformation
     * @param after the transformation to apply
     * @return a new finder that applies this finder and then applies the transformation
     */
    default <R> Function<T, R> transformResult(Function<Boolean, R> after) {
        return t -> after.apply(this.find(t));
    }

    /**
     * Returns a finder that always returns true.
     *
     * @param <T> the type of object to find
     * @return a finder that always returns true
     */
    static <T> Finder<T> always() {
        return t -> true;
    }

    /**
     * Returns a finder that always returns false.
     *
     * @param <T> the type of object to find
     * @return a finder that always returns false
     */
    static <T> Finder<T> never() {
        return t -> false;
    }

    /**
     * Creates a finder from a predicate.
     *
     * @param <T> the type of object to find
     * @param predicate the predicate to convert
     * @return a finder that applies the predicate
     */
    static <T> Finder<T> fromPredicate(Predicate<T> predicate) {
        return predicate::test;
    }

    /**
     * Returns a finder that finds objects by a specific property.
     *
     * @param <T> the type of object to find
     * @param <R> the type of the property
     * @param propertyExtractor function to extract the property from the object
     * @param propertyFinder finder for the property
     * @return a finder that finds objects by the specified property
     */
    static <T, R> Finder<T> byProperty(Function<T, R> propertyExtractor, Finder<R> propertyFinder) {
        return t -> propertyFinder.find(propertyExtractor.apply(t));
    }

    /**
     * Returns a finder that finds objects by a specific property value.
     *
     * @param <T> the type of object to find
     * @param <R> the type of the property
     * @param propertyExtractor function to extract the property from the object
     * @param value the value to compare with
     * @return a finder that finds objects by the specified property value
     */
    static <T, R> Finder<T> byPropertyEquals(Function<T, R> propertyExtractor, R value) {
        return t -> {
            R propertyValue = propertyExtractor.apply(t);
            return propertyValue != null && propertyValue.equals(value);
        };
    }

    /**
     * Returns a finder that finds objects by a specific property value when the property is optional.
     *
     * @param <T> the type of object to find
     * @param <R> the type of the property
     * @param propertyExtractor function to extract the optional property from the object
     * @param value the value to compare with
     * @return a finder that finds objects by the specified optional property value
     */
    static <T, R> Finder<T> byOptionalProperty(Function<T, Optional<R>> propertyExtractor, R value) {
        return t -> propertyExtractor.apply(t).map(v -> v.equals(value)).orElse(false);
    }
}
