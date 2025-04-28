# Dependencies TOML Configuration

This folder contains TOML configuration files that organize all dependencies from the Common module by category. These files make it easier to import specific dependency groups into other projects.

## Usage

In your project, you can use these TOML files to selectively import dependencies:

```java
TOMLParser parser = new TOMLParser();
File tomlFile = new File("path/to/database.toml");
Toml toml = parser.parse(tomlFile);

// Access dependency information
TomlTable databaseDeps = toml.getTable("dependencies");
for (String key : databaseDeps.keySet()) {
    String groupId = databaseDeps.getTable(key).getString("groupId");
    String artifactId = databaseDeps.getTable(key).getString("artifactId");
    String version = databaseDeps.getTable(key).getString("version");
    
    // Use dependency information in your build system
    System.out.println(groupId + ":" + artifactId + ":" + version);
}
```

## Available Categories

- `core.toml` - Core dependencies
- `database.toml` - Database related dependencies
- `cache.toml` - Caching dependencies
- `monitoring.toml` - Monitoring and logging dependencies
- `testing.toml` - Testing dependencies
- `tools.toml` - Utility and support tools
- `spring.toml` - Spring framework dependencies
- `spring-cloud.toml` - Spring Cloud dependencies
- `security.toml` - Security related dependencies
- `messaging.toml` - Messaging and event processing dependencies

## Integration with Gradle

You can create a Gradle plugin that reads these TOML files and adds dependencies to your project:

```groovy
class TomlDependenciesPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('tomlDependencies', TomlDependenciesExtension)
        
        project.afterEvaluate {
            if (project.tomlDependencies.tomlFile) {
                Toml toml = new Toml().read(project.tomlDependencies.tomlFile)
                TomlTable deps = toml.getTable("dependencies")
                
                deps.keySet().each { String key ->
                    TomlTable dep = deps.getTable(key)
                    project.dependencies {
                        implementation "${dep.getString("groupId")}:${dep.getString("artifactId")}:${dep.getString("version")}"
                    }
                }
            }
        }
    }
}

class TomlDependenciesExtension {
    File tomlFile
}
```