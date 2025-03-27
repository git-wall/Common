# Common Library

This is a common library project that provides shared functionality for Spring-based microservices.

## Features & Optimize

* Algorithms: DAG, LevenshteinSearch
* Benchmark: JMH
* BRC: for handler millions data in file
* Cache: Two-level caching with Redis and local cache, Google Cache, Hazelcast
* Tech support config: cassandra, elastic, couchbase, redis, scylla
* Health check endpoints
* EAV: support pattern for handler field outside to class 
* Interceptor monitor request and response processing
* Jooq: support code gen entities
* Kafka:
  * Kafka Spring with 1 broker 
  * Kafka Custom with multi broker
* Multion: support register map enum & factory entities of interface  
* Notifications: Line, Email, Telegram
* Patterns:
  * Legacy: Behavior Chain, Pipeline, Singleton, ServiceLocator, etc...
  * Revisited: etc...
  * Test: AAA, ObjectMother, PresentationModel
* Thread cover 
* Utils cover etc

## Reference Documentation

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.18/gradle-plugin/reference/html/)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/index.html#actuator)
* [Spring Cache Abstraction](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/index.html#io.caching)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/index.html#web)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/index.html#messaging.kafka)
* [Validation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/htmlsingle/index.html#io.validation)

## Project Structure

The project is organized into several packages:

* `org.app.common.algorithms` - Algorithms suport
* `org.app.common.base` - Base classes for controllers and services
* `org.app.common.benchmark`- Support test in code
* `org.app.common.cache` - Caching implementations including TwoLevelCache
* `org.app.common.data` - Data handling utilities
* `org.app.common.health` - Health check implementations
* `org.app.common.kafka` - Kafka messaging utilities
* `org.app.common.multion` - Multion pattern implementation
* `org.app.common.eav` - EAV pattern database implementation
* `org.app.common.interceptor` - Interceptor request, response, monitor
* `org.app.common.pattern` - Design pattern implementations
* `org.app.common.notification` - Notification support Line, Email, Telegram
* `org.app.common.utils` - Utils support easy way to use
* `org.app.common.thread` - Manager thread, create ExecutorService best way
* `org.app.common.validation` - Valid support way

### Health Checks

The project includes sever health check implementations:
* `CacheHealthCheck` - Verifies Redis connectivity
* `DbHealthCheck` - Verifies database connectivity
* `SystemHealthCheck` - Reports system metrics

### Caching

`TwoLevelCache` provides a two-level caching mechanism with:
* Local in-memory cache for fast access
* Redis-based distributed cache for shared state

### Design Patterns

* `MultionType` - A factory pattern using enums to define types
* `EAV` - Database design pattern
* `Pipeline` - Action by action
* etc...

### Tip Single

```java
// Good: Async initialization
@Async
@EventListener(ApplicationReadyEvent.class)
public void init() {
    // Heavy initialization
}

// Good: Streaming file processing
@PostMapping("/upload")
public void handleFileUpload(@RequestParam("file") MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
        // Process stream in chunks
    }
}

// Good: Bounded cache with eviction
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10)));
        return cacheManager;
    }
}

// Good: Bounded cache with eviction
@Component
public class UserCache {
    private final Cache<String, User> cache;

    public UserCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }
}
```

```properties
// Enable compression
server:
compression:
enabled: true
mime-types: text/html,text/xml,text/plain,text/css,application/javascript,application/json
min-response-size: 1024
```

#### Tip group
```properties
// Enable lazy initialization
spring:
  main:
    lazy-initialization: true
```

```java
// Selective lazy initialization
@Configuration
public class ServiceConfig {
    @Lazy
    @Bean
    public ExpensiveService expensiveService() {
        return new ExpensiveService();
    }
}
```

## Building
```shell
   ./gradlew build
```

## Publishing
- need nexus service
```yaml
version: '3.8'
services:

  nexus:
    image: sonatype/nexus3
    expose:
      - 8081
    ports:
      - "8381:8081"
    restart: always 
```
- add this with name [gradle.properties]
```properties
SNAPSHOT_REPOSITORY_URL=http://localhost:8381/repository/maven-snapshots/
NEXUS_USERNAME=...
NEXUS_PASSWORD=...
```
- run this after nexus service start
```shell
   ./gradlew publish
```

