# Messaging Templates

This directory contains templates for setting up messaging systems and implementing common messaging patterns in your Spring Boot applications.

## Contents

### Kafka
- `broker-config.properties`: Configuration for Kafka brokers
- `topic-creation.yml`: Kubernetes custom resources for creating Kafka topics

### RabbitMQ
- `rabbitmq.conf`: Configuration for RabbitMQ server
- `definitions.json`: RabbitMQ definitions including exchanges, queues, and bindings

### Schemas
- Avro:
  - `user-event.avsc`: Avro schema for user events
- Protobuf:
  - `order-event.proto`: Protocol Buffers schema for order events

### Patterns
- `pub-sub.yml`: Configuration for implementing the Publish-Subscribe pattern
- `request-reply.yml`: Configuration for implementing the Request-Reply pattern

## Usage

### Kafka

1. Configure Kafka brokers using the provided `broker-config.properties` template.
2. Create Kafka topics using the `topic-creation.yml` template with Strimzi Kafka operator:
   ```bash
   kubectl apply -f topic-creation.yml
   ```
RabbitMQ

```bash
rabbitmqctl import_definitions definitions.json
```

Schemas

```xml
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.11.0</version>
</dependency>
<dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-avro-serializer</artifactId>
    <version>7.0.1</version>
</dependency>
```