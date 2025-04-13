package org.app.common.test.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ObjectMother<T> {
    private final Map<String, Supplier<T>> templates = new HashMap<>(5, 0.75f);
    private final Map<String, UnaryOperator<T>> customizers = new HashMap<>(5, 0.75f);

    public void register(String name, Supplier<T> template) {
        templates.put(name, template);
    }

    public void registerCustomizer(String name, UnaryOperator<T> customizer) {
        customizers.put(name, customizer);
    }

    public T create(String templateName) {
        if (!templates.containsKey(templateName)) {
            throw new IllegalArgumentException("Unknown template: " + templateName);
        }
        return templates.get(templateName).get();
    }

    public T createCustomized(String templateName, String customizerName) {
        T object = create(templateName);
        if (customizers.containsKey(customizerName)) {
            return customizers.get(customizerName).apply(object);
        }
        return object;
    }
}