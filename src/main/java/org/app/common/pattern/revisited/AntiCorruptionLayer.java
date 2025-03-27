package org.app.common.pattern.revisited;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AntiCorruptionLayer<S, T> {
    private final Map<Class<?>, Function<S, ?>> translators = new HashMap<>();
    private final Map<Class<?>, Function<?, S>> reverseTranslators = new HashMap<>();

    public <R> void registerTranslator(Class<R> targetType, 
                                     Function<S, R> translator,
                                     Function<R, S> reverseTranslator) {
        translators.put(targetType, translator);
        reverseTranslators.put(targetType, reverseTranslator);
    }

    @SuppressWarnings("unchecked")
    public <R> R translate(S source, Class<R> targetType) {
        Function<S, ?> translator = translators.get(targetType);
        if (translator == null) {
            throw new IllegalArgumentException("No translator for type: " + targetType);
        }
        return (R) translator.apply(source);
    }

    @SuppressWarnings("unchecked")
    public <R> S reverseTranslate(R target, Class<R> sourceType) {
        Function<R, S> reverseTranslator = (Function<R, S>) reverseTranslators.get(sourceType);
        if (reverseTranslator == null) {
            throw new IllegalArgumentException("No reverse translator for type: " + sourceType);
        }
        return reverseTranslator.apply(target);
    }
}