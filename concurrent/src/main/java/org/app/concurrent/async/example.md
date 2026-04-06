# AsyncProcessor Usage Guide

This guide provides practical examples of how to use the `AsyncProcessor` class for handling asynchronous operations in your application.

## Table of Contents

1. [Introduction](#introduction)
2. [Basic Operations](#basic-operations)
3. [Chaining Operations](#chaining-operations)
4. [Parallel Execution](#parallel-execution)
5. [Error Handling](#error-handling)
6. [Timeouts and Fallbacks](#timeouts-and-fallbacks)
7. [Retry Mechanisms](#retry-mechanisms)
8. [Circuit Breaker Pattern](#circuit-breaker-pattern)
9. [Caching Results](#caching-results)
10. [Rate Limiting](#rate-limiting)
11. [Bulkhead Pattern](#bulkhead-pattern)
12. [Complete Examples](#complete-examples)

## Introduction

The `AsyncProcessor` class provides a comprehensive set of utilities for working with asynchronous operations using Java's `CompletableFuture` API. It simplifies common patterns like:

- Executing tasks asynchronously
- Chaining multiple operations
- Running tasks in parallel
- Handling errors and timeouts
- Implementing resilience patterns (retry, circuit breaker, etc.)

## Basic Operations

### Simple Asynchronous Execution

```java
@Autowired
// Execute a task that returns a value
CompletableFuture<String> future = asyncProcessor.executeAsync(() -> {
    // Simulate work
    Thread.sleep(1000);
    return "Hello, World!";
});

// Process the result when it's available
future.thenAccept(result -> System.out.println("Result: " + result));

// Execute a task with no return value
CompletableFuture<Void> voidFuture = asyncProcessor.executeAsync(() -> {
    // Simulate work
    Thread.sleep(1000);
    System.out.println("Task completed!");
});
```

## Chaining Operations

### Sequential Processing
```java
// Chain multiple transformations on an initial value
CompletableFuture<Integer> result = asyncProcessor.chain(
    "Hello",                              // Initial value
    s -> s + ", World!",                  // First transformation
    s -> s.length(),                      // Second transformation
    length -> length * 2                  // Third transformation
);

// result will eventually contain 24 (length of "Hello, World!" * 2)

// Chain operations on an existing CompletableFuture
CompletableFuture<String> initialFuture = asyncProcessor.executeAsync(() -> "Hello");
CompletableFuture<Integer> chainedResult = asyncProcessor.chain(
    initialFuture,
    s -> s + ", World!",
    s -> s.length()
); 
```
### Dependent Tasks
```java
// Execute two tasks in sequence where the second depends on the first
CompletableFuture<Integer> result = asyncProcessor.executeSequence(
    () -> "Hello, World!",                // First task
    s -> s.length()                       // Second task that uses the result of the first
);
```
### Parallel Execution
#### Running Multiple Tasks
```java
// Execute multiple tasks in parallel and collect all results
List<Supplier<String>> tasks = Arrays.asList(
    () -> "Task 1 result",
    () -> "Task 2 result",
    () -> "Task 3 result"
);

CompletableFuture<List<String>> allResults = asyncProcessor.executeAll(tasks);

// Process all results when available
allResults.thenAccept(results -> {
    results.forEach(System.out::println);
});

// Execute multiple tasks and process results as they complete
asyncProcessor.executeAllAndConsume(
    tasks,
    result -> System.out.println("Completed: " + result)
);
```

#### Combining Results
```java
// Execute two tasks in parallel and combine their results
CompletableFuture<String> combined = asyncProcessor.executeBoth(
    () -> "Hello",                                    // First task
    () -> "World",                                    // Second task
    (result1, result2) -> result1 + ", " + result2    // How to combine results
);
```

#### First Completed Task
```java
// Execute multiple tasks and take the result of the first one to complete
List<Supplier<String>> tasks = Arrays.asList(
    () -> {
        Thread.sleep(2000);
        return "Slow task";
    },
    () -> {
        Thread.sleep(1000);
        return "Medium task";
    },
    () -> {
        Thread.sleep(500);
        return "Fast task";
    }
);

CompletableFuture<String> firstResult = asyncProcessor.executeAny(tasks);
// firstResult will contain "Fast task"
```

### Error Handling
#### Using Success and Failure Handlers
```java
asyncProcessor.executeWithHandlers(
    () -> {
        if (Math.random() > 0.5) {
            return "Success";
        } else {
            throw new RuntimeException("Random failure");
        }
    },
    result -> System.out.println("Success: " + result),
    error -> System.err.println("Failed: " + error.getMessage())
);
```

#### Using Fallback Function
```java
CompletableFuture<String> result = asyncProcessor.executeWithFallbackFn(
    () -> {
        if (Math.random() > 0.5) {
            return "Success";
        } else {
            throw new RuntimeException("Random failure");
        }
    },
    error -> "Fallback: " + error.getMessage()
);
```

### Timeouts and Fallbacks

#### Adding Timeouts
```java
// Execute a task with a timeout
CompletableFuture<String> result = asyncProcessor.executeWithTimeout(
    () -> {
        Thread.sleep(2000);
        return "Completed";
    },
    1,                  // Timeout duration
    TimeUnit.SECONDS    // Time unit
);
// This will complete exceptionally with a TimeoutException

// Execute a task with a deadline (absolute time)
long deadline = System.currentTimeMillis() + 1000; // 1 second from now
CompletableFuture<String> deadlineResult = asyncProcessor.executeWithDeadline(
    () -> {
        Thread.sleep(2000);
        return "Completed";
    },
    deadline
);
```

#### Using Fallbacks
```java
// Execute a task with a fallback value if it fails or times out
CompletableFuture<String> result = asyncProcessor.executeWithFallback(
    () -> {
        Thread.sleep(2000);
        return "Completed";
    },
    "Fallback value",   // Fallback value
    1,                  // Timeout duration
    TimeUnit.SECONDS    // Time unit
);
// result will contain "Fallback value" if the task times out
```

### Retry Mechanisms

#### Implementing Retry Logic
```java
// Execute a task with retry if it fails
CompletableFuture<String> result = asyncProcessor.executeWithRetry(
    () -> {
        if (Math.random() > 0.7) {
            return "Success after retry";
        } else {
            throw new RuntimeException("Random failure");
        }
    },
    3,                  // Maximum number of retries
    500,                // Delay between retries
    TimeUnit.MILLISECONDS
);
```

#### Using Exponential Backoff
```java
// Execute a task with exponential backoff retry
CompletableFuture<String> result = asyncProcessor.executeWithExponentialBackoff(
    () -> {
        if (Math.random() > 0.7) {
            return "Success after retry";
        } else {
            throw new RuntimeException("Random failure");
        }
    },
    5,                  // Maximum number of retries
    100,                // Initial delay
    5000,               // Maximum delay
    TimeUnit.MILLISECONDS
);
```

### Circuit Breaker Pattern

#### Implementing Circuit Breaker Logic

```java
// Execute a task with a circuit breaker
CompletableFuture<String> result = asyncProcessor.executeWithCircuitBreaker(
    () -> {
        if (Math.random() > 0.7) {
            return "Success";
        } else {
            throw new RuntimeException("Service unavailable");
        }
    },
    "Fallback when circuit is open",  // Fallback value
    3,                                // Failure threshold
    5,                                // Reset timeout
    TimeUnit.SECONDS                  // Time unit
);
```

### Caching Results

#### Implementing Caching Logic

```java
// Execute a task and cache the result
CompletableFuture<String> result1 = asyncProcessor.executeWithCache(
    () -> {
        System.out.println("Executing expensive operation");
        return "Cached result";
    },
    "cacheKey1",        // Cache key
    30,                 // Cache duration
    TimeUnit.SECONDS    // Time unit
);

// This will use the cached result if called within 30 seconds
CompletableFuture<String> result2 = asyncProcessor.executeWithCache(
    () -> {
        System.out.println("This won't be printed if cache is hit");
        return "Cached result";
    },
    "cacheKey1",
    30,
    TimeUnit.SECONDS
);
```

### Rate Limiting
#### Implementing Rate Limiting Logic

```java
// Execute tasks with rate limiting
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    asyncProcessor.executeWithRateLimit(
        () -> {
            System.out.println("Executing task " + taskId);
            return "Result " + taskId;
        },
        1,              // Permits required for this task
        5.0             // Maximum permits per second
    );
}
```

### Bulkhead Pattern
#### Implementing Bulkhead Logic

```java
// Execute tasks with bulkhead pattern (limiting concurrent executions)
for (int i = 0; i < 20; i++) {
    final int taskId = i;
    asyncProcessor.executeWithBulkhead(
        () -> {
            System.out.println("Executing task " + taskId);
            Thread.sleep(1000);
            return "Result " + taskId;
        },
        5,              // Maximum concurrent executions
        10              // Maximum waiting tasks
    );
}
```

## Complete Examples

### Web Service Client with Resilience

```java
@Service
public class ResilientWebClient {
    
    private final AsyncProcessor asyncProcessor;
    private final WebClient webClient;
    
    public ResilientWebClient(AsyncProcessor asyncProcessor, WebClient webClient) {
        this.asyncProcessor = asyncProcessor;
        this.webClient = webClient;
    }
    
    public CompletableFuture<String> fetchDataWithResilience(String url) {
        return asyncProcessor.executeWithCircuitBreaker(
            () -> fetchData(url),
            "Service temporarily unavailable",  // Fallback
            5,                                  // Failure threshold
            30,                                 // Reset timeout
            TimeUnit.SECONDS                    // Time unit
        );
    }
    
    private String fetchData(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocking for simplicity in this example
    }
}
```

### Batch Processing with Parallel Execution

```java
@Service
public class BatchProcessor {
    
    private final AsyncProcessor asyncProcessor;
    private final DataRepository repository;
    
    public BatchProcessor(AsyncProcessor asyncProcessor, DataRepository repository) {
        this.asyncProcessor = asyncProcessor;
        this.repository = repository;
    }
    
    public CompletableFuture<List<ProcessingResult>> processBatch(List<String> items) {
        List<Supplier<ProcessingResult>> tasks = items.stream()
                .map(item -> (Supplier<ProcessingResult>) () -> processItem(item))
                .collect(Collectors.toList());
        
        return asyncProcessor.executeAll(tasks);
    }
    
    private ProcessingResult processItem(String item) {
        // Simulate processing
        try {
            Thread.sleep((long) (Math.random() * 1000));
            return new ProcessingResult(item, "Processed: " + item);
        } catch (Exception e) {
            return new ProcessingResult(item, "Failed: " + e.getMessage());
        }
    }
    
    public static class ProcessingResult {
        private final String itemId;
        private final String result;
        
        public ProcessingResult(String itemId, String result) {
            this.itemId = itemId;
            this.result = result;
        }
        
        // Getters omitted for brevity
    }
}
```

### Caching Service

```java
@Service
public class CachingService {
    
    private final AsyncProcessor asyncProcessor;
    private final ExpensiveOperationService service;
    
    public CachingService(AsyncProcessor asyncProcessor, ExpensiveOperationService service) {
        this.asyncProcessor = asyncProcessor;
        this.service = service;
    }
    
    public CompletableFuture<Data> getData(String id) {
        return asyncProcessor.executeWithCache(
            () -> service.fetchData(id),
            "data:" + id,       // Cache key
            5,                  // Cache duration
            TimeUnit.MINUTES    // Time unit
        );
    }
}
```
