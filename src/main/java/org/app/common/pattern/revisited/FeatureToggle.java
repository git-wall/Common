package org.app.common.pattern.revisited;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class FeatureToggle {
    private final Map<String, Boolean> features = new ConcurrentHashMap<>();
    private final Map<String, Supplier<Boolean>> dynamicFeatures = new ConcurrentHashMap<>();

    public void enable(String featureName) {
        features.put(featureName, true);
    }

    public void disable(String featureName) {
        features.put(featureName, false);
    }

    public void registerDynamic(String featureName, Supplier<Boolean> evaluator) {
        dynamicFeatures.put(featureName, evaluator);
    }

    public boolean isEnabled(String featureName) {
        return dynamicFeatures.containsKey(featureName) 
            ? dynamicFeatures.get(featureName).get() 
            : features.getOrDefault(featureName, false);
    }

    public <T> T execute(String featureName, Supplier<T> enabled, Supplier<T> disabled) {
        return isEnabled(featureName) ? enabled.get() : disabled.get();
    }
}