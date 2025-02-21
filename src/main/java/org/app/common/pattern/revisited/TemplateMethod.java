package org.app.common.pattern.revisited;

import org.springframework.lang.NonNull;

import java.util.function.Function;
/**
 * <blockquote><pre>{@code
 *  TemplateMethod
 *  .action(p -> Map.of("eventTicket", "I'm going!").values().stream().findFirst().get())
 *  .executeFrom(passInformation);
 * }
 * </blockquote></pre>
 * */
public class TemplateMethod<T, R> {
    private final Function<T, R> strategy;

    protected TemplateMethod(Function<T, R> strategy) {
        this.strategy = strategy;
    }

    @NonNull
    public static <T, R> TemplateMethod<T, R> action(Function<T, R> strategy) {
        return new TemplateMethod<>(strategy);
    }

    public R executeFrom(T t) {
        return strategy.apply(t);
    }
}
