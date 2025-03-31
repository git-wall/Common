# Spring Common Library

A comprehensive Spring Boot library providing common utilities, components, and integrations for enterprise applications.

## Overview

This library serves as a foundation for Spring Boot applications, offering a wide range of ready-to-use components and utilities. It simplifies development by providing implementations for common patterns, database integrations, caching solutions, and more.

## Features

- **Database Integrations**: Support for multiple database technologies
  - JPA/Hibernate
  - MyBatis
  - JOOQ
  - Cassandra
  - Scylla
  - Couchbase
  - Elasticsearch

- **Caching Solutions**:
  - Hazelcast
  - Redis/Valkey
  - Caffeine

- **Messaging and Event Processing**:
  - Kafka support
  - Spring Cloud Stream
  - Parallel consumer processing

- **Resilience Patterns**:
  - Circuit Breaker with Resilience
  - Saga pattern implementation
  - Transaction management

- **Monitoring and Observability**:
  - Logging with SLF4J and Logback
  - Micrometer metrics
  - Distributed tracing with Sleuth and Zipkin

- **API Documentation**:
  - SpringDoc OpenAPI integration

- **Utility Classes**:
  - Common patterns and helpers
  - Validation utilities
  - Exception handling

- **Testing Support**:
  - JUnit integration
  - Testcontainers for integration tests
  - JaCoCo for test coverage

## Getting Started

### Prerequisites

- Java 11 or higher
- Gradle 7.x or higher

### Including the Library

`build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven {
        url "YOUR_REPOSITORY_URL"
        credentials {
            username "YOUR_USERNAME"
            password "YOUR_PASSWORD"
        }
    }
}

dependencies {
  implementation 'com.core:Common:1.0.0-SNAPSHOT'
}
```

`pom.xml`:
```xml
<dependency>
    <groupId>com.core</groupId>
    <artifactId>Common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Building from Source

To build the library from source:

```bash
./gradlew clean build
```

To run tests:

```bash
./gradlew test
```

To generate test coverage report:

```bash
./gradlew jacocoTestReport
```

## Contributing

Please follow these steps to contribute:

1. Fork the repository
2. Create a feature branch
3. Add your changes
4. Run tests
5. Submit a pull request

## License

    1.0.0

## Contact

    Email:thanhbp696@gmail.com
    Phone:0363166463
    LinkedIn:https://www.linkedin.com/in/th%C3%A0nh-nguy%E1%BB%85n-588618218/