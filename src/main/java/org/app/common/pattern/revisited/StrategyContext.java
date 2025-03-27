package org.app.common.pattern.revisited;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrategyContext<I, O> {
    private final Map<String, Strategy<I, O>> strategies;
    @Getter
    private Strategy<I, O> currentStrategy;

    public StrategyContext() {
        this.strategies = new ConcurrentHashMap<>(16);
    }

    public void registerStrategy(String name, Strategy<I, O> strategy) {
        strategies.put(name, strategy);
    }

    public void setCurrentStrategy(String name) {
        Strategy<I, O> strategy = strategies.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy not found: " + name);
        }
        this.currentStrategy = strategy;
    }

    public O execute(I input) {
        if (currentStrategy == null) {
            throw new IllegalStateException("No strategy selected");
        }
        return currentStrategy.execute(input);
    }
}

