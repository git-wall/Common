# Testing Templates

This directory contains templates for various testing approaches for Spring Boot applications.

## Contents

### Integration Testing
- `rest-assured-template.java`: Template for API integration testing using REST Assured
- `testcontainers-config.java`: Configuration for integration testing with Testcontainers

### Load Testing
- JMeter:
  - `load-test-plan.jmx`: JMeter test plan for load testing REST APIs
- Gatling:
  - `simulation.scala`: Gatling simulation for load testing REST APIs

### Chaos Testing
- `chaos-monkey-config.yml`: Configuration for Chaos Monkey for Spring Boot
- `chaos-mesh-experiment.yml`: Chaos Mesh experiments for Kubernetes environments

### Test Data
- `test-data-generator.java`: Utility for generating realistic test data
- `test-fixtures.json`: Sample test fixtures for testing

## Usage

### Integration Testing

1. Add the following dependencies to your Spring Boot project:
```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>