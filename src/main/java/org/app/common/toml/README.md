```java
public class TomlExample {

    /**
     * Demonstrates how to use the TomlConfig class to load and access TOML
     * configuration values.
     */
    public static void demonstrateTomlConfig() {
        // Load TOML from classpath
        TomlConfig config = TomlConfig.loadFromClasspath("dependencies/cache.toml");

        // Get a string value
        String description = config.getString("dependencies.hazelcast.description");
        System.out.println("Hazelcast description: " + description);

        // Get a section
        TomlConfig hazelcastSection = config.getSection("dependencies.hazelcast");
        if (hazelcastSection != null) {
            String groupId = hazelcastSection.getString("groupId");
            String artifactId = hazelcastSection.getString("artifactId");
            String version = hazelcastSection.getString("version");

            System.out.println("Hazelcast dependency: " + groupId + ":" + artifactId + ":" + version);
        }

        // Check if a path exists
        boolean hasJedis = config.hasPath("dependencies.jedis");
        System.out.println("Has Jedis dependency: " + hasJedis);
    }

    /**
     * Demonstrates how to use the DependencyTomlLoader class to load and access
     * dependency information.
     */
    public static void demonstrateDependencyLoader() {
        // Load dependencies from classpath
        DependencyTomlLoader loader = DependencyTomlLoader.fromClasspath("dependencies/cache.toml");

        // Get all dependencies
        Map<String, DependencyTomlLoader.DependencyInfo> dependencies = loader.getDependencies();
        System.out.println("Found " + dependencies.size() + " dependencies");

        // Get a specific dependency
        DependencyTomlLoader.DependencyInfo caffeine = loader.getDependency("caffeine");
        if (caffeine != null) {
            System.out.println("Caffeine dependency: " + caffeine.getMavenCoordinate());
            System.out.println("Description: " + caffeine.getDescription());
        }

        // Print all dependencies
        System.out.println("\nAll dependencies:");
        dependencies.forEach((name, info) -> {
            System.out.println(name + ": " + info.getMavenCoordinate());
        });
    }

    /**
     * Main method to run the examples.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("=== TomlConfig Example ===");
        demonstrateTomlConfig();

        System.out.println("\n=== DependencyTomlLoader Example ===");
        demonstrateDependencyLoader();
    }
}
```