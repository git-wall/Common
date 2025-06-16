package org.app.common.design.revisited;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SubclassSandbox<T, R> {
    private final Map<String, Function<T, R>> operations = new HashMap<>();

    protected void registerOperation(String name, Function<T, R> operation) {
        operations.put(name, operation);
    }

    protected R execute(String operationName, T input) {
        Function<T, R> operation = operations.get(operationName);
        if (operation == null) {
            throw new IllegalArgumentException("Operation not found: " + operationName);
        }
        return operation.apply(input);
    }

    protected boolean hasOperation(String name) {
        return operations.containsKey(name);
    }
}