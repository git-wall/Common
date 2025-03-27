package org.app.common.pattern.revisited;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class Servant<T> {
    private final Function<T, Boolean> validator;

    public Servant(Function<T, Boolean> validator) {
        this.validator = validator;
    }

    public void serve(Collection<T> serviced, Consumer<T> service) {
        serviced.stream()
                .filter(validator::apply)
                .forEach(service);
    }

    public <R> Collection<R> serveAndCollect(
            Collection<T> serviced, 
            Function<T, R> service,
            Collection<R> results) {
        serviced.stream()
                .filter(validator::apply)
                .map(service)
                .forEach(results::add);
        return results;
    }
}