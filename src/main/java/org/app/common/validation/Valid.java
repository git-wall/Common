package org.app.common.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
/**
 * <pre>{@code
 * var user = new User("user", 24, Sex.FEMALE, "foobar.com");
 * Valid.of(user)
 *     .valid(User::name, Objects::nonNull, "name is null")
 *     .valid(User::name, name -> !name.isEmpty(), "name is empty")
 *     .valid(User::email, email -> !email.contains("@"), "email not contains '@'")
 *     .valid(User::age, age -> age > 20 && age < 30, "age isn't between...")
 *     .get();
 * }
 * </pre>
 * */
public class Valid<T> {
    private final T obj;
    private final List<Throwable> exceptions = new ArrayList<>();

    private Valid(T obj) {
        this.obj = obj;
    }

    public static <T> Valid<T> of(T t) {
        return new Valid<>(Objects.requireNonNull(t));
    }

    public Valid<T> valid(Predicate<? super T> validation, String message) {
        if (!validation.test(obj)) {
            exceptions.add(new IllegalStateException(message));
        }
        return this;
    }

    public <U> Valid<T> validate(
            Function<? super T, ? extends U> function,
            Predicate<? super U> validation,
            String message
    ) {
        return valid(function.andThen(validation::test)::apply, message);
    }

    public T get() throws IllegalStateException {
        if (exceptions.isEmpty()) {
            return obj;
        }
        var e = new IllegalStateException();
        exceptions.forEach(e::addSuppressed);
        throw e;
    }
}
