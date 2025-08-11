package org.app.common.pipeline.v1;

import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A versatile pipeline implementation for handling various data structures
 * including single elements, collections, maps, arrays, and iterables.
 *
 * @param <T> The type of elements in the pipeline
 */
public class Pipeline<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final Pipeline<?> EMPTY = new Pipeline<>();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    private Pipeline() {
        this.value = null;
    }

    /**
     * Returns an empty {@code Pipeline} instance.  No value is present for this
     * {@code Pipeline}.
     *
     * @param <T> The type of the non-existent value
     * @return an empty {@code Pipeline}
     * @apiNote Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code Pipeline.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     */
    public static <T> Pipeline<T> empty() {
        @SuppressWarnings("unchecked")
        Pipeline<T> t = (Pipeline<T>) EMPTY;
        return t;
    }

    /**
     * Constructs an instance with the described value.
     *
     * @param value the non-{@code null} value to describe
     * @throws NullPointerException if value is {@code null}
     */
    private Pipeline(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Returns an {@code Pipeline} describing the given non-{@code null}
     * value.
     *
     * @param value the value to describe, which must be non-{@code null}
     * @param <T>   the type of the value
     * @return an {@code Pipeline} with the value present
     * @throws NullPointerException if value is {@code null}
     */
    public static <T> Pipeline<T> of(T value) {
        return new Pipeline<>(value);
    }

    /**
     * Returns an {@code Pipeline} describing the given value, if
     * non-{@code null}, otherwise returns an empty {@code Pipeline}.
     *
     * @param value the possibly-{@code null} value to describe
     * @param <T>   the type of the value
     * @return an {@code Pipeline} with a present value if the specified value
     * is non-{@code null}, otherwise an empty {@code Pipeline}
     */
    public static <T> Pipeline<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Pipeline}
     * @throws NoSuchElementException if no value is present
     * @apiNote The preferred alternative to this method is {@link #orElseThrow()}.
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * If a value is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a value not present, returns {@code true}, otherwise
     * {@code false}.
     *
     * @return {@code true} if a value is not present, otherwise {@code false}
     * @since 11
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Throws an exception if the value is null.
     *
     * @param message The exception message to use if the value is null.
     * @return The current Pipeline instance.
     * @throws IllegalArgumentException if the value is null.
     */
    public Pipeline<T> ifNullThrow(String message) {
        Assert.notNull(value, message);
        return this;
    }

    public <X extends Throwable> Pipeline<T> ifNullThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    public <X extends Throwable> Pipeline<T> ifEmptyThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value == null) {
            throw exceptionSupplier.get();
        } else if (value instanceof String) {
            if (((String) value).isEmpty()) {
                throw exceptionSupplier.get();
            }
        } else if (value.getClass().isArray()) {
            if (((Object[]) value).length == 0) {
                throw exceptionSupplier.get();
            }
        } else if (value instanceof Map) {
            if (((Map<?, ?>) value).isEmpty()) {
                throw exceptionSupplier.get();
            }
        } else if (value instanceof Collection) {
            if (((Collection<?>) value).isEmpty()) {
                throw exceptionSupplier.get();
            }
        }
        return this;
    }

    public Pipeline<T> ifEmptyThrow(String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        } else if (value instanceof String) {
            Assert.hasText((String) value, message);
        } else if (value.getClass().isArray()) {
            Assert.notEmpty((Object[]) value, message);
        } else if (value instanceof Map) {
            Assert.notEmpty((Map<?, ?>) value, message);
        } else if (value instanceof Collection) {
            Assert.notEmpty((Collection<?>) value, message);
        }
        return this;
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     * @throws NullPointerException if value is present and the given action is
     *                              {@code null}
     */
    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action      the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     *                    present
     * @throws NullPointerException if a value is present and the given action
     *                              is {@code null}, or no value is present and the given empty-based
     *                              action is {@code null}.
     * @since 9
     */
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code Pipeline} describing the value, otherwise returns an
     * empty {@code Pipeline}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code Pipeline} describing the value of this
     * {@code Pipeline}, if a value is present and the value matches the
     * given predicate, otherwise an empty {@code Pipeline}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public Pipeline<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    /**
     * If a value is present, returns an {@code Pipeline} describing (as if by
     * {@link #ofNullable}) the result of applying the given mapping function to
     * the value, otherwise returns an empty {@code Pipeline}.
     *
     * <p>If the mapping function returns a {@code null} result then this method
     * returns an empty {@code Pipeline}.
     *
     * @param mapper the mapping function to apply to a value, if present
     * @param <U>    The type of the value returned from the mapping function
     * @return an {@code Pipeline} describing the result of applying a mapping
     * function to the value of this {@code Pipeline}, if a value is
     * present, otherwise an empty {@code Pipeline}
     * @throws NullPointerException if the mapping function is {@code null}
     * @apiNote This method supports post-processing on {@code Pipeline} values, without
     * the need to explicitly check for a return status.  For example, the
     * following code traverses a stream of URIs, selects one that has not
     * yet been processed, and creates a path from that URI, returning
     * an {@code Pipeline<Path>}:
     *
     * <pre>{@code
     *     Pipeline<Path> p =
     *         uris.stream().filter(uri -> !isProcessedYet(uri))
     *                       .findFirst()
     *                       .map(Paths::get);
     * }</pre>
     * <p>
     * Here, {@code findFirst} returns an {@code Pipeline<URI>}, and then
     * {@code map} returns an {@code Pipeline<Path>} for the desired
     * URI if one exists.
     */
    public <U> Pipeline<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Pipeline.ofNullable(mapper.apply(value));
        }
    }

    /**
     * If a value is present, returns the result of applying the given
     * {@code Pipeline}-bearing mapping function to the value, otherwise returns
     * an empty {@code Pipeline}.
     *
     * <p>This method is similar to {@link #map(Function)}, but the mapping
     * function is one whose result is already an {@code Pipeline}, and if
     * invoked, {@code flatMap} does not wrap it within an additional
     * {@code Pipeline}.
     *
     * @param <U>    The type of value of the {@code Pipeline} returned by the
     *               mapping function
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Pipeline}-bearing mapping
     * function to the value of this {@code Pipeline}, if a value is
     * present, otherwise an empty {@code Pipeline}
     * @throws NullPointerException if the mapping function is {@code null} or
     *                              returns a {@code null} result
     */
    public <U> Pipeline<U> flatMap(Function<? super T, ? extends Pipeline<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            @SuppressWarnings("unchecked")
            Pipeline<U> r = (Pipeline<U>) mapper.apply(value);
            return Objects.requireNonNull(r);
        }
    }

    /**
     * If a value is present, returns an {@code Pipeline} describing the value,
     * otherwise returns an {@code Pipeline} produced by the supplying function.
     *
     * @param supplier the supplying function that produces an {@code Pipeline}
     *                 to be returned
     * @return returns an {@code Pipeline} describing the value of this
     * {@code Pipeline}, if a value is present, otherwise an
     * {@code Pipeline} produced by the supplying function.
     * @throws NullPointerException if the supplying function is {@code null} or
     *                              produces a {@code null} result
     * @since 9
     */
    public Pipeline<T> or(Supplier<? extends Pipeline<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            Pipeline<T> r = (Pipeline<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing
     * only that value, otherwise returns an empty {@code Stream}.
     *
     * @return the Pipeline value as a {@code Stream}
     * @apiNote This method can be used to transform a {@code Stream} of Pipeline
     * elements to a {@code Stream} of present value elements:
     * <pre>{@code
     *     Stream<Pipeline<T>> os = ..
     *     Stream<T> s = os.flatMap(Pipeline::stream)
     * }</pre>
     * @since 9
     */
    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *              May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     * supplying function
     * @throws NullPointerException if no value is present and the supplying
     *                              function is {@code null}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Pipeline}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public T orElseThrow() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @param <X>               Type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an
     *                          exception to be thrown
     * @return the value, if present
     * @throws X                    if no value is present
     * @throws NullPointerException if no value is present and the exception
     *                              supplying function is {@code null}
     * @apiNote A method reference to the exception constructor with an empty argument
     * list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Pipeline}.
     * The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Pipeline} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Pipeline)) {
            return false;
        }

        Pipeline<?> other = (Pipeline<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code of the value, if present, otherwise {@code 0}
     * (zero) if no value is present.
     *
     * @return hash code value of the present value or {@code 0} if no value is
     * present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this {@code Pipeline}
     * suitable for debugging.  The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @return the string representation of this instance
     * @implSpec If a value is present the result must include its string representation
     * in the result.  Empty and present {@code Pipeline}s must be unambiguously
     * differentiable.
     */
    @Override
    public String toString() {
        return value != null
                ? String.format("Pipeline[%s]", value)
                : "Pipeline.empty";
    }
}