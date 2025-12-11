# Boot and Run
1. Create application-local profile for fast testing
- application-local.properties or application-local.yml in src/main/resources
- add gitignore entry for application-local.*
```properties
  debug=true
  logging.level.root=DEBUG
  feature.fastTest=true
  spring.main.lazy-initialization=true
  # remove auto configurations you don't need testing
  autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration\
  org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

2. IntelliJ IDEA
- Program args: --rule.debug=true --cache.mode=local-only
- Environment variables: SPRING_PROFILES_ACTIVE=local or -Dspring.profiles.active=local
- Run/Debug Configurations -> modify options -> Enable debug output, hide banner, Open run/debug tool window when started, 
3. Gradle
settings.gradle
```groovy
  buildCache {
  local {
    enabled = true
  }
}
```
gradle.properties:
```properties
org.gradle.configureondemand=true
org.gradle.parallel=true
org.gradle.caching=true
# just compile any changes like IntelliJ IDEA incremental build
org.gradle.unsafe.configuration-cache=true
org.gradle.compiler.incremental=true
```

build.gradle
```groovy
  developmentOnly 'org.springframework.boot:spring-boot-devtools'
```
