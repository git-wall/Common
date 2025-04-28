package org.app.common.toml;

import com.moandjiezana.toml.Toml;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A utility class for loading and accessing TOML configuration files.
 * <p>
 * This class provides a simple API for loading TOML files and accessing their
 * values
 * with support for type conversion, default values, and path-based value
 * access.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * TomlConfig config = TomlConfig.load("config.toml");
 * String value = config.getString("section.key");
 * Integer intValue = config.getInteger("section.intKey", 0); // with default value
 * List<String> list = config.getList("section.listKey");
 * </pre>
 */
@Getter
public class TomlConfig {

    private final Toml toml;

    /**
     * Creates a new TomlConfig instance with the given Toml object.
     *
     * @param toml the Toml object
     */
    private TomlConfig(Toml toml) {
        this.toml = toml;
    }

    /**
     * Loads a TOML configuration file from the given file path.
     *
     * @param filePath the path to the TOML file
     * @return a new TomlConfig instance
     * @throws TomlException if the file cannot be loaded or parsed
     */
    public static TomlConfig load(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new TomlException("TOML file not found: " + filePath);
            }
            return new TomlConfig(new Toml().read(file));
        } catch (Exception e) {
            throw new TomlException("Failed to load TOML file: " + filePath, e);
        }
    }

    /**
     * Loads a TOML configuration file from the given file.
     *
     * @param file the TOML file
     * @return a new TomlConfig instance
     * @throws TomlException if the file cannot be loaded or parsed
     */
    public static TomlConfig load(File file) {
        try {
            if (!file.exists()) {
                throw new TomlException("TOML file not found: " + file.getPath());
            }
            return new TomlConfig(new Toml().read(file));
        } catch (Exception e) {
            throw new TomlException("Failed to load TOML file: " + file.getPath(), e);
        }
    }

    /**
     * Loads a TOML configuration from the given input stream.
     *
     * @param inputStream the input stream containing TOML data
     * @return a new TomlConfig instance
     * @throws TomlException if the stream cannot be parsed
     */
    public static TomlConfig load(InputStream inputStream) {
        try {
            return new TomlConfig(new Toml().read(inputStream));
        } catch (Exception e) {
            throw new TomlException("Failed to load TOML from input stream", e);
        }
    }

    /**
     * Loads a TOML configuration from the classpath.
     *
     * @param resourcePath the path to the resource on the classpath
     * @return a new TomlConfig instance
     * @throws TomlException if the resource cannot be loaded or parsed
     */
    public static TomlConfig loadFromClasspath(String resourcePath) {
        try (InputStream is = TomlConfig.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new TomlException("TOML resource not found on classpath: " + resourcePath);
            }
            return load(is);
        } catch (IOException e) {
            throw new TomlException("Failed to load TOML resource from classpath: " + resourcePath, e);
        }
    }

    /**
     * Gets a string value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.key")
     * @return the string value, or null if not found
     */
    public String getString(String path) {
        return getString(path, null);
    }

    /**
     * Gets a string value from the TOML configuration with a default value.
     *
     * @param path         the path to the value (e.g., "section.key")
     * @param defaultValue the default value to return if the path is not found
     * @return the string value, or the default value if not found
     */
    public String getString(String path, String defaultValue) {
        return getValueByPath(path, String.class, defaultValue);
    }

    /**
     * Gets an integer value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.key")
     * @return the integer value, or null if not found
     */
    public Integer getInteger(String path) {
        return getInteger(path, null);
    }

    /**
     * Gets an integer value from the TOML configuration with a default value.
     *
     * @param path         the path to the value (e.g., "section.key")
     * @param defaultValue the default value to return if the path is not found
     * @return the integer value, or the default value if not found
     */
    public Integer getInteger(String path, Integer defaultValue) {
        return getValueByPath(path, Integer.class, defaultValue);
    }

    /**
     * Gets a long value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.key")
     * @return the long value, or null if not found
     */
    public Long getLong(String path) {
        return getLong(path, null);
    }

    /**
     * Gets a long value from the TOML configuration with a default value.
     *
     * @param path         the path to the value (e.g., "section.key")
     * @param defaultValue the default value to return if the path is not found
     * @return the long value, or the default value if not found
     */
    public Long getLong(String path, Long defaultValue) {
        return getValueByPath(path, Long.class, defaultValue);
    }

    /**
     * Gets a double value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.key")
     * @return the double value, or null if not found
     */
    public Double getDouble(String path) {
        return getDouble(path, null);
    }

    /**
     * Gets a double value from the TOML configuration with a default value.
     *
     * @param path         the path to the value (e.g., "section.key")
     * @param defaultValue the default value to return if the path is not found
     * @return the double value, or the default value if not found
     */
    public Double getDouble(String path, Double defaultValue) {
        return getValueByPath(path, Double.class, defaultValue);
    }

    /**
     * Gets a boolean value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.key")
     * @return the boolean value, or null if not found
     */
    public Boolean getBoolean(String path) {
        return getBoolean(path, null);
    }

    /**
     * Gets a boolean value from the TOML configuration with a default value.
     *
     * @param path         the path to the value (e.g., "section.key")
     * @param defaultValue the default value to return if the path is not found
     * @return the boolean value, or the default value if not found
     */
    public Boolean getBoolean(String path, Boolean defaultValue) {
        return getValueByPath(path, Boolean.class, defaultValue);
    }

    /**
     * Gets a list value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.listKey")
     * @param <T>  the type of elements in the list
     * @return the list value, or an empty list if not found
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path) {
        try {
            Object value = getValueFromPath(path);
            if (value instanceof List) {
                return (List<T>) value;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Gets a map value from the TOML configuration.
     *
     * @param path the path to the value (e.g., "section.mapKey")
     * @param <K>  the type of keys in the map
     * @param <V>  the type of values in the map
     * @return the map value, or an empty map if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(String path) {
        try {
            Object value = getValueFromPath(path);
            if (value instanceof Map) {
                return (Map<K, V>) value;
            }
            return Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Gets a TomlConfig instance for a nested section.
     *
     * @param path the path to the section (e.g., "section")
     * @return a new TomlConfig instance for the section, or null if not found
     */
    public TomlConfig getSection(String path) {
        try {
            Toml section = toml.getTable(path);
            if (section != null) {
                return new TomlConfig(section);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a path exists in the TOML configuration.
     *
     * @param path the path to check
     * @return true if the path exists, false otherwise
     */
    public boolean hasPath(String path) {
        try {
            String[] parts = path.split("\\.");
            Toml current = toml;

            for (int i = 0; i < parts.length - 1; i++) {
                current = current.getTable(parts[i]);
                if (current == null) {
                    return false;
                }
            }

            return current.contains(parts[parts.length - 1]);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets a value from the TOML configuration by path with type conversion.
     *
     * @param path         the path to the value
     * @param type         the type to convert to
     * @param defaultValue the default value to return if the path is not found
     * @param <T>          the type parameter
     * @return the value, or the default value if not found
     */
    @SuppressWarnings("unchecked")
    private <T> T getValueByPath(String path, Class<T> type, T defaultValue) {
        try {
            Object value = getValueFromPath(path);
            if (value == null) {
                return defaultValue;
            }

            if (type.isInstance(value)) {
                return (T) value;
            }

            // Handle type conversion
            if (type == String.class) {
                return (T) value.toString();
            } else if (type == Integer.class && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            } else if (type == Long.class && value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            } else if (type == Double.class && value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            } else if (type == Boolean.class && value instanceof String) {
                return (T) Boolean.valueOf(value.toString());
            }

            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a value from the TOML configuration by path.
     *
     * @param path the path to the value
     * @return the value, or null if not found
     */
    private Object getValueFromPath(String path) {
        String[] parts = path.split("\\.");

        if (parts.length == 1) {
            return toml.toMap().get(parts[0]);
        }

        Toml current = toml;
        for (int i = 0; i < parts.length - 1; i++) {
            current = current.getTable(parts[i]);
            if (current == null) {
                return null;
            }
        }

        return current.toMap().get(parts[parts.length - 1]);
    }
}