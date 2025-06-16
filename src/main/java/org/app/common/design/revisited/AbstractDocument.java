package org.app.common.design.revisited;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AbstractDocument {
    void put(String key, Object value);
    Object get(String key);
    <T> Stream<T> children(String key, Function<Map<String, Object>, T> constructor);
}

class DefaultDocument implements AbstractDocument {
    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public void put(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<T> children(String key, Function<Map<String, Object>, T> constructor) {
        return Optional.ofNullable(get(key))
                .filter(List.class::isInstance)
                .stream()
                .flatMap(list -> ((List<Map<String, Object>>) list).stream().map(constructor));
    }
}

interface HasType extends AbstractDocument {
    default String getType() {
        return (String) get("type");
    }
}

interface HasPrice extends AbstractDocument {
    default Number getPrice() {
        return (Number) get("price");
    }
}

interface HasParts extends AbstractDocument {
    default Stream<Part> getParts() {
        return children("parts", Part::new);
    }
}

class Part extends DefaultDocument implements HasType, HasPrice {
    public Part(Map<String, Object> properties) {
        properties.forEach(this::put);
    }
}

class Product extends DefaultDocument implements HasType, HasPrice, HasParts {
    public Product(Map<String, Object> properties) {
        properties.forEach(this::put);
    }
}