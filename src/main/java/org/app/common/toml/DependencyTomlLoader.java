package org.app.common.toml;

import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for loading and accessing dependency information from TOML
 * configuration files.
 * <p>
 * This class is specifically designed to work with the dependency TOML files in
 * the project,
 * providing easy access to dependency information such as groupId, artifactId,
 * version, and description.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * DependencyTomlLoader loader = DependencyTomlLoader.fromClasspath("dependencies/cache.toml");
 * Map<String, DependencyInfo> dependencies = loader.getDependencies();
 *
 * // Get a specific dependency
 * DependencyInfo hazelcast = loader.getDependency("hazelcast");
 * String version = hazelcast.getVersion();
 * }
 * </pre>
 */
public class DependencyTomlLoader {

    private final TomlConfig tomlConfig;
    private final Map<String, DependencyInfo> dependencies;

    /**
     * Creates a new DependencyTomlLoader with the given TomlConfig.
     *
     * @param tomlConfig the TomlConfig instance
     */
    private DependencyTomlLoader(TomlConfig tomlConfig) {
        this.tomlConfig = tomlConfig;
        this.dependencies = loadDependencies();
    }

    /**
     * Loads a dependency TOML file from the given file path.
     *
     * @param filePath the path to the TOML file
     * @return a new DependencyTomlLoader instance
     * @throws TomlException if the file cannot be loaded or parsed
     */
    public static DependencyTomlLoader fromFile(String filePath) {
        return new DependencyTomlLoader(TomlConfig.load(filePath));
    }

    /**
     * Loads a dependency TOML file from the given file.
     *
     * @param file the TOML file
     * @return a new DependencyTomlLoader instance
     * @throws TomlException if the file cannot be loaded or parsed
     */
    public static DependencyTomlLoader fromFile(File file) {
        return new DependencyTomlLoader(TomlConfig.load(file));
    }

    /**
     * Loads a dependency TOML file from the classpath.
     *
     * @param resourcePath the path to the resource on the classpath
     * @return a new DependencyTomlLoader instance
     * @throws TomlException if the resource cannot be loaded or parsed
     */
    public static DependencyTomlLoader fromClasspath(String resourcePath) {
        return new DependencyTomlLoader(TomlConfig.loadFromClasspath(resourcePath));
    }

    /**
     * Gets all dependencies from the TOML file.
     *
     * @return a map of dependency names to DependencyInfo objects
     */
    public Map<String, DependencyInfo> getDependencies() {
        return new HashMap<>(dependencies);
    }

    /**
     * Gets a specific dependency by name.
     *
     * @param name the name of the dependency
     * @return the DependencyInfo for the dependency, or null if not found
     */
    public DependencyInfo getDependency(String name) {
        return dependencies.get(name);
    }

    /**
     * Loads all dependencies from the TOML file.
     *
     * @return a map of dependency names to DependencyInfo objects
     */
    private Map<String, DependencyInfo> loadDependencies() {
        Map<String, DependencyInfo> result = new HashMap<>();
        TomlConfig depsSection = tomlConfig.getSection("resources/dependencies");

        if (depsSection == null) {
            return result;
        }

        Map<String, Object> depsMap = depsSection.getToml().toMap();
        for (String key : depsMap.keySet()) {
            TomlConfig depConfig = depsSection.getSection(key);
            if (depConfig != null) {
                String groupId = depConfig.getString("groupId");
                String artifactId = depConfig.getString("artifactId");
                String version = depConfig.getString("version");
                String description = depConfig.getString("description");

                result.put(key, new DependencyInfo(groupId, artifactId, version, description));
            }
        }

        return result;
    }

    /**
     * A class representing dependency information.
     */
    @Getter
    public static class DependencyInfo {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String description;

        /**
         * Creates a new DependencyInfo with the given values.
         *
         * @param groupId     the group ID
         * @param artifactId  the artifact ID
         * @param version     the version
         * @param description the description
         */
        public DependencyInfo(String groupId, String artifactId, String version, String description) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.description = description;
        }

        /**
         * Gets the Maven coordinate string (groupId:artifactId:version).
         *
         * @return the Maven coordinate string
         */
        public String getMavenCoordinate() {
            return groupId + ":" + artifactId + ":" + version;
        }

        @Override
        public String toString() {
            return "DependencyInfo{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
