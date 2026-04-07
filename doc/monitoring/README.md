# Monitoring Templates

This directory contains templates for setting up monitoring, logging, and tracing for your Spring Boot applications in Kubernetes.

## Contents

### Prometheus
- `prometheus.yml`: Configuration for Prometheus server
- `alert-rules.yml`: Alert rules for Spring Boot applications

### Grafana
- Dashboards:
  - `jvm-dashboard.json`: Dashboard for JVM metrics
  - `kubernetes-dashboard.json`: Dashboard for Kubernetes metrics
  - `spring-boot-dashboard.json`: Dashboard for Spring Boot metrics
- Datasources:
  - `prometheus-datasource.yml`: Configuration for Prometheus datasource

### Logging
- Fluentd:
  - `fluentd.conf`: Configuration for Fluentd log collector
- Elasticsearch:
  - `elasticsearch.yml`: Configuration for Elasticsearch

### Tracing
- `jaeger.yml`: Kubernetes deployment for Jaeger
- `zipkin.yml`: Kubernetes deployment for Zipkin

## Usage

These templates can be used to set up a complete monitoring stack for your Spring Boot applications. Here's how to use them:

1. Deploy Prometheus and Grafana using the provided configurations
2. Set up logging with Fluentd and Elasticsearch
3. Choose either Jaeger or Zipkin for distributed tracing
4. Configure your Spring Boot applications to expose metrics, logs, and traces

For Spring Boot applications, add the following dependencies to your `pom.xml`:

```xml
<!-- Metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Tracing -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

```properties
# Metrics
management.endpoints.web.exposure.include=prometheus,health,info,metrics
management.endpoint.health.show-details=always
management.metrics.tags.application=${spring.application.name}

# Tracing
spring.sleuth.sampler.probability=1.0
spring.zipkin.base-url=http://zipkin:9411
```