package org.app.common.design.revisited;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Command<T, R> {
    private final Map<String, Function<T, R>> commands = new HashMap<>(8);
    private final Consumer<Exception> errorHandler;

    public Command(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void register(String name, Function<T, R> command) {
        commands.put(name, command);
    }

    public R execute(String name, T input) {
        try {
            return commands.getOrDefault(name, t -> {
                throw new IllegalArgumentException("Command not found: " + name);
            }).apply(input);
        } catch (Exception e) {
            errorHandler.accept(e);
            throw e;
        }
    }
}