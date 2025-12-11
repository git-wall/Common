package org.app.common.beam.io;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration options for Beam IO operations.
 * This class provides a flexible way to configure IO operations with different parameters.
 */
public class IOOptions implements Serializable {

    private static final long serialVersionUID = -2214208861012556373L;

    private final transient Map<String, Object> options;

    /**
     * Default constructor.
     */
    public IOOptions() {
        this.options = new HashMap<>();
    }

    /**
     * Constructor with initial options.
     *
     * @param options Initial options map
     */
    public IOOptions(Map<String, Object> options) {
        this.options = new HashMap<>(options);
    }

    /**
     * Set an option value.
     *
     * @param key   The option key
     * @param value The option value
     * @return This IOOptions instance for method chaining
     */
    public IOOptions set(String key, Object value) {
        options.put(key, value);
        return this;
    }

    /**
     * Get an option value.
     *
     * @param key The option key
     * @param <T> The expected type of the option value
     * @return The option value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) options.get(key);
    }

    /**
     * Get an option value with a default value if not found.
     *
     * @param key          The option key
     * @param defaultValue The default value to return if the key is not found
     * @param <T>          The expected type of the option value
     * @return The option value, or the default value if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) options.getOrDefault(key, defaultValue);
    }

    /**
     * Check if an option exists.
     *
     * @param key The option key
     * @return true if the option exists, false otherwise
     */
    public boolean has(String key) {
        return options.containsKey(key);
    }

    /**
     * Remove an option.
     *
     * @param key The option key
     * @return This IOOptions instance for method chaining
     */
    public IOOptions remove(String key) {
        options.remove(key);
        return this;
    }

    /**
     * Get all options.
     *
     * @return The option map
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(options);
    }
}
