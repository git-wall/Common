# gRPC Module Documentation
This document provides a guide on how to use the gRPC module in your Spring Boot applications.

## Overview
The gRPC module provides a set of utilities and configurations to easily integrate gRPC services into your Spring Boot applications. It includes:

- Auto-configuration for gRPC servers and clients
- Base classes for implementing gRPC services and clients
- Interceptors for logging, validation, security, and metrics
- Exception handling utilities

## Getting Started

### 1. Add Dependencies

Add the following dependencies to your project:

```xml
<dependency>
    <groupId>org.app</groupId>
    <artifactId>common</artifactId>
    <version>${common.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>${grpc.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>${grpc.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>${grpc.version}</version>
</dependency>
```

### 2. Configure gRPC Properties

```yaml
grpc:
  server:
    port: 9090
    enabled: true
    maxInboundMessageSize: 4194304  # 4MB
    maxInboundMetadataSize: 8192    # 8KB
  client:
    host: localhost
    port: 9090
    deadlineMs: 5000
    keepAliveEnabled: true
    keepAliveTimeMs: 30000
    keepAliveTimeoutMs: 5000
  auth:
    enabled: false
    tokenSecret: your-secret-key
    tokenExpirationMs: 3600000
```

### 3. Enable Auto-Configuration

```java
import org.app.common.grpc.EnableGrpc;

@SpringBootApplication
@EnableGrpc
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 4. Define Your Proto File
Create a proto file in src/main/proto

```proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.example.grpc";

package example;

service ExampleService {
  rpc GetExample (ExampleRequest) returns (ExampleResponse);
}

message ExampleRequest {
  string id = 1;
}

message ExampleResponse {
  string id = 1;
  string name = 2;
}
```

### 5. Implement the Service

```java
@Slf4j
@Service
public class ExampleGrpcService 
        extends AbstractGrpcService<ExampleServiceGrpc.ExampleServiceImplBase> 
    implements GrpcServiceDefinition {

    @Override
    public ServerServiceDefinition getServiceDefinition() {
        return new ExampleServiceGrpc.ExampleServiceImplBase() {
            @Override
            public void getExample(ExampleRequest request, StreamObserver<ExampleResponse> responseObserver) {
                try {
                    log.info("Received request for example with ID: {}", request.getId());
                    
                    // Process the request
                    ExampleResponse response = ExampleResponse.newBuilder()
                            .setId(request.getId())
                            .setName("Example " + request.getId())
                            .build();
                    
                    // Complete the call
                    complete(response, responseObserver);
                } catch (Exception e) {
                    handleException(e, responseObserver);
                }
            }
        }.bindService();
    }

    @Override
    public String getServiceName() {
        return ExampleServiceGrpc.SERVICE_NAME;
    }
}
```

## Implementing gRPC Clients

```java
@Slf4j
@Service
public class ExampleGrpcClient extends AbstractGrpcClient {

    private final ExampleServiceGrpc.ExampleServiceBlockingStub blockingStub;
    private final ExampleServiceGrpc.ExampleServiceFutureStub futureStub;

    @Autowired
    public ExampleGrpcClient(ManagedChannel channel, @Value("${grpc.client.deadlineMs}") long deadlineMs) {
        super(channel, deadlineMs);
        this.blockingStub = ExampleServiceGrpc.newBlockingStub(channel);
        this.futureStub = ExampleServiceGrpc.newFutureStub(channel);
    }

    public ExampleResponse getExample(String id) {
        ExampleRequest request = ExampleRequest.newBuilder().setId(id).build();
        
        return executeWithRetry(() -> {
            log.info("Sending gRPC request for example with ID: {}", id);
            return blockingStub
                    .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                    .getExample(request);
        });
    }

    public ListenableFuture<ExampleResponse> getExampleAsync(String id) {
        ExampleRequest request = ExampleRequest.newBuilder().setId(id).build();
        
        return futureStub
                .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                .getExample(request);
    }
}
```

### Exception Handling
The handleException method in AbstractGrpcService will convert the exception to an appropriate gRPC status code:

- IllegalArgumentException → INVALID_ARGUMENT
- IllegalStateException → FAILED_PRECONDITION
- UnsupportedOperationException → UNIMPLEMENTED
- NullPointerException → INTERNAL
- Other exceptions → UNKNOWN
```java
try {
    // Your service logic
} catch (Exception e) {
    handleException(e, responseObserver);
}
```

## Interceptors
The module includes several interceptors that can be used to add cross-cutting concerns to your gRPC services:

### 1. LoggingInterceptor
Logs gRPC requests and responses.

### 2. ValidationInterceptor
Validates incoming gRPC requests.

### 3. SecurityInterceptor
Adds authentication and authorization to gRPC services.

### 4. MetricsInterceptor
Collects metrics for gRPC calls.

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── Application.java
│   │           ├── grpc/
│   │           │   ├── client/
│   │           │   │   └── ExampleGrpcClient.java
│   │           │   └── service/
│   │           │       └── ExampleGrpcService.java
│   │           └── rest/
│   │               └── ExampleController.java
│   ├── proto/
│   │   └── example.proto
│   └── resources/
│       └── application.yml