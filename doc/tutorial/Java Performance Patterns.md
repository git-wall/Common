# Java Performance Patterns and Anti-Patterns

## Table of Contents
1. [Memory Patterns](#memory-patterns)
2. [Concurrency Patterns](#concurrency-patterns)
3. [Database Patterns](#database-patterns)
4. [Application Patterns](#application-patterns)
5. [JVM Tuning Patterns](#jvm-tuning-patterns)
6. [Network Patterns](#network-patterns)
7. [Serialization Patterns](#serialization-patterns)
8. [Logging Patterns](#logging-patterns)
9. [Exception Handling Patterns](#exception-handling-patterns)
10. [Memory Leak Prevention](#memory-leak-prevention)
11. [Startup Optimization](#startup-optimization)
12. [Reactive Programming Patterns](#reactive-programming-patterns)
13. [Microservices Performance Patterns](#microservices-performance-patterns)
14. [Cloud-Native Performance Patterns](#cloud-native-performance-patterns)

## Memory Patterns

### String Management
**✅ Good Patterns:**
```java
// String Pool Usage
String str1 = "hello";  // Uses string pool
String str2 = "hello";  // Reuses same instance

// StringBuilder for loops
StringBuilder builder = new StringBuilder(expectedSize);
for (Item item : items) {
    builder.append(item.getName())
           .append(",");
}

// String.format for complex formatting
String message = String.format("User %s logged in at %s", 
    username, timestamp);
```

**❌ Anti-Patterns:**
```java
// Avoid string concatenation in loops
String result = "";
for (Item item : items) {
    result += item.getName() + ",";  // Creates many objects
}

// Avoid unnecessary String object creation
String str = new String("hello");  // Don't do this
```

### Collection Usage
**✅ Good Patterns:**
```java
// Initialize with known capacity
List<String> items = new ArrayList<>(expectedSize);
Map<String, User> userMap = new HashMap<>(expectedSize, 0.75f);

// Use appropriate collection type
Set<String> uniqueItems = new HashSet<>();  // For unique items
Queue<Task> tasks = new LinkedBlockingQueue<>(1000);  // For producer-consumer

// Bulk operations
list.addAll(newItems);  // Better than individual adds
collection.removeAll(itemsToRemove);  // Bulk remove
```

**❌ Anti-Patterns:**
```java
// Avoid growing collections repeatedly
List<String> list = new ArrayList<>();  // No initial capacity
for (int i = 0; i < 10000; i++) {
    list.add(String.valueOf(i));  // Many resizing operations
}

// Avoid wrong collection type
List<String> uniqueItems = new ArrayList<>();
if (!uniqueItems.contains(item)) {  // O(n) operation
    uniqueItems.add(item);
}
```

## Concurrency Patterns

### Thread Pool Management
**✅ Good Patterns:**
```java
// Properly sized thread pools
int processors = Runtime.getRuntime().availableProcessors();
ExecutorService computePool = Executors.newFixedThreadPool(processors);
ExecutorService ioPool = Executors.newFixedThreadPool(processors * 2);

// Proper shutdown
try {
    pool.shutdown();
    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();
    }
} catch (InterruptedException e) {
    pool.shutdownNow();
    Thread.currentThread().interrupt();
}
```

**❌ Anti-Patterns:**
```java
// Avoid unbounded thread creation
for (Task task : tasks) {
    new Thread(task).start();  // Can exhaust system resources
}

// Avoid mixing synchronized and Lock APIs
synchronized(lock) {
    reentrantLock.lock();  // Potential deadlock
    try {
        // logic
    } finally {
        reentrantLock.unlock();
    }
}
```

## Database Patterns

### Connection Management
**✅ Good Patterns:**
```java
// Connection pool configuration
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(10);
config.setMinimumIdle(5);
config.setIdleTimeout(300000);
config.setConnectionTimeout(20000);

// Batch operations
@Transactional
public void batchInsert(List<User> users) {
    for (int i = 0; i < users.size(); i++) {
        entityManager.persist(users.get(i));
        if (i % 50 == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid N+1 queries
@OneToMany(fetch = FetchType.EAGER)  // Causes N+1 problem
private List<Order> orders;

// Avoid large in-memory result sets
Query query = session.createQuery("from User");
List<User> users = query.list();  // Loads all users into memory
```

## Application Patterns

### Caching Strategies
**✅ Good Patterns:**
```java
// Two-level caching
@Cacheable(value = "users", unless = "#result == null")
@CachePut(value = "users", key = "#user.id")
public User saveUser(User user) {
    return userRepository.save(user);
}

// Cache with TTL
Cache<String, User> cache = Caffeine.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .maximumSize(10_000)
    .build();
```

**❌ Anti-Patterns:**
```java
// Avoid caching everything
@Cacheable("allData")  // Could consume too much memory
public List<Data> getAllData() {
    return repository.findAll();
}

// Avoid unnecessary cache invalidation
@CacheEvict(value = "users", allEntries = true)  // Too broad
public void updateUser(User user) {
    repository.save(user);
}
```

## JVM Tuning Patterns

### Memory Settings
**✅ Good Patterns:**
```bash
# Container-aware settings
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:InitialRAMPercentage=50.0 \
     -jar app.jar

# G1GC for general use
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=16M \
     -jar app.jar

# ZGC for low latency
java -XX:+UseZGC \
     -Xmx16g \
     -XX:+AlwaysPreTouch \
     -jar app.jar
```

### Monitoring Patterns
**✅ Good Patterns:**
```bash
# Comprehensive monitoring
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/dumps \
     -XX:+UseGCLogFileRotation \
     -XX:NumberOfGCLogFiles=5 \
     -XX:GCLogFileSize=20M \
     -Xlog:gc*=info:file=/logs/gc.log \
     -jar app.jar

# JFR for production monitoring
java -XX:StartFlightRecording=disk=true,\
maxage=2h,maxsize=1g,\
settings=profile,\
filename=recording.jfr \
-jar app.jar
```

### Resource Management
**✅ Good Patterns:**
```java
// AutoCloseable resources
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(SQL);
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        // Process results
    }
}

// Resource pools
GenericObjectPool<Socket> pool = new GenericObjectPool<>(factory);
pool.setMaxTotal(100);
pool.setMaxIdle(20);
pool.setMinIdle(10);
```

**❌ Anti-Patterns:**
```java
// Avoid manual resource management
Connection conn = null;
try {
    conn = getConnection();
    // Use connection
} finally {
    if (conn != null) {
        try {
            conn.close();
        } catch (SQLException e) {
            // Log error
        }
    }
}

// Avoid unbounded resource usage
Cache<String, byte[]> cache = Caffeine.newBuilder()
    .maximumSize(Integer.MAX_VALUE)  // No upper bound
    .build();
```

## Network Patterns

### HTTP Client Management
**✅ Good Patterns:**
```java
// Reusable HTTP client
private static final HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .executor(Executors.newFixedThreadPool(10))
    .build();

// Async operations
CompletableFuture<String> future = client.sendAsync(request, 
    HttpResponse.BodyHandlers.ofString())
    .thenApply(HttpResponse::body);
```

**❌ Anti-Patterns:**
```java
// Don't create new client for each request
HttpClient client = HttpClient.newHttpClient();  // Expensive
String response = client.send(request, 
    HttpResponse.BodyHandlers.ofString()).body();

// Avoid blocking operations in event loops
CompletableFuture.supplyAsync(() -> {
    Thread.sleep(1000);  // Blocks thread pool
    return "result";
});
```

## Serialization Patterns

### JSON Processing
**✅ Good Patterns:**
```java
// Reuse ObjectMapper
private static final ObjectMapper mapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

// Streaming for large JSON
try (JsonParser parser = mapper.getFactory().createParser(file)) {
    while (parser.nextToken() != null) {
        // Process tokens
    }
}
```

**❌ Anti-Patterns:**
```java
// Don't create new mapper for each operation
ObjectMapper mapper = new ObjectMapper();  // Expensive
String json = mapper.writeValueAsString(object);

// Avoid loading large JSON into memory
String largeJson = Files.readString(path);  // Memory intensive
JsonNode root = mapper.readTree(largeJson);
```

## Logging Patterns

### Efficient Logging
**✅ Good Patterns:**
```java
// Guard expensive logging
if (log.isDebugEnabled()) {
    log.debug("Complex calculation result: {}", 
        expensiveOperation());
}

// Use parameterized logging
log.info("User {} performed action {}", 
    username, action);

// Structured logging
log.info("Transaction processed", 
    Map.of(
        "id", txId,
        "amount", amount,
        "status", status
    ));
```

**❌ Anti-Patterns:**
```java
// Avoid string concatenation in logging
log.debug("Result: " + expensiveOperation());  // Always evaluated

// Avoid excessive logging
for (Item item : items) {
    log.debug("Processing item: {}", item);  // Log spam
}
```

## Exception Handling Patterns

### Performance-Aware Exception Handling
**✅ Good Patterns:**
```java
// Cache exception for frequent use
private static final NotFoundException NOT_FOUND = 
    new NotFoundException("Entity not found");

// Use exception hierarchies
public class BusinessException extends RuntimeException {
    private final ErrorCode code;
    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid creating new exceptions in hot paths
throw new NotFoundException("Entity " + id + " not found");  // Creates new exception

// Don't use exceptions for flow control
try {
    return map.get(key);
} catch (NullPointerException e) {
    return defaultValue;
}
```

## Memory Leak Prevention

### Resource Cleanup
**✅ Good Patterns:**
```java
// Proper event listener cleanup
public class EventListener {
    private final List<WeakReference<Handler>> handlers = 
        new CopyOnWriteArrayList<>();
    
    public void addHandler(Handler handler) {
        handlers.add(new WeakReference<>(handler));
    }
    
    public void cleanup() {
        handlers.removeIf(ref -> ref.get() == null);
    }
}

// ThreadLocal cleanup
private static final ThreadLocal<UserContext> userContext = 
    ThreadLocal.withInitial(UserContext::new);

@PreDestroy
public void cleanup() {
    userContext.remove();
}
```

**❌ Anti-Patterns:**
```java
// Memory leak in static collections
private static final List<Session> sessions = 
    new ArrayList<>();  // Never cleaned up

// Unclosed resources in finally block
InputStream is = new FileInputStream(file);
try {
    // Process stream
} finally {
    // Missing is.close()
}
```

## Startup Optimization

### Lazy Initialization
**✅ Good Patterns:**
```java
// Lazy singleton
public class ExpensiveService {
    private static class Holder {
        static final ExpensiveService INSTANCE = 
            new ExpensiveService();
    }
    
    public static ExpensiveService getInstance() {
        return Holder.INSTANCE;
    }
}

// Lazy collection initialization
private List<Item> items;

public List<Item> getItems() {
    if (items == null) {
        items = new ArrayList<>();
    }
    return items;
}
```

**❌ Anti-Patterns:**
```java
// Eager initialization of unused resources
private final ExpensiveResource resource = 
    new ExpensiveResource();  // Might not be needed

// Loading all data at startup
@PostConstruct
public void init() {
    loadAllDataFromDatabase();  // Delays startup
}
```

## Reactive Programming Patterns

### Reactive Streams
**✅ Good Patterns:**
```java
// Use non-blocking operations
Flux.fromIterable(items)
    .flatMap(item -> callExternalService(item)  // Async operation
        .subscribeOn(Schedulers.boundedElastic()))
    .collectList()
    .block();

// Backpressure handling
Flux.fromIterable(largeDataSet)
    .onBackpressureBuffer(10000)
    .publishOn(Schedulers.boundedElastic())
    .subscribe(this::processItem);

// Reactive error handling
Mono.just("data")
    .flatMap(this::processAsync)
    .timeout(Duration.ofSeconds(5))
    .onErrorResume(TimeoutException.class, 
        e -> Mono.just(fallback))
    .retry(3);
```

**❌ Anti-Patterns:**
```java
// Avoid blocking operations in reactive streams
Flux.fromIterable(items)
    .map(item -> {
        Thread.sleep(100);  // Blocking operation!
        return process(item);
    });

// Don't mix blocking and reactive code
Flux.fromIterable(items)
    .map(item -> jdbcTemplate.queryForObject(...))  // Blocking!
    .subscribe(result -> log.info("Got: {}", result));

// Avoid unnecessary subscription
flux.subscribe();  // Subscription without handling results
```

### Reactive Performance Optimization
**✅ Good Patterns:**
```java
// Parallel processing with controlled concurrency
Flux.fromIterable(items)
    .parallel(Runtime.getRuntime().availableProcessors())
    .runOn(Schedulers.boundedElastic())
    .map(this::processItem)
    .sequential();

// Caching reactive results
Mono<Data> cachedData = Mono.fromCallable(this::fetchData)
    .cache(Duration.ofMinutes(5));

// Batching operations
Flux.fromIterable(items)
    .buffer(100)  // Process in batches of 100
    .flatMap(batch -> processBatch(batch)
        .subscribeOn(Schedulers.boundedElastic()));
```

**❌ Anti-Patterns:**
```java
// Avoid excessive parallelization
Flux.fromIterable(items)
    .parallel(1000)  // Too many parallel streams
    .runOn(Schedulers.boundedElastic());

// Don't ignore backpressure
source.subscribe(new BaseSubscriber<>() {
    @Override
    protected void hookOnNext(Data data) {
        requestUnbounded();  // Requests unlimited items
    }
});
```

## Microservices Performance Patterns

### Circuit Breaker Pattern
**✅ Good Patterns:**
```java
// Using Resilience4j Circuit Breaker
CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendService");
CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

// Decorate service calls
public Mono<Response> callService() {
    return CircuitBreaker.decorateCallable(circuitBreaker, 
        () -> backendService.call())
        .andThen(response -> {
            metrics.record(response);
            return response;
        });
}

// Configure with proper thresholds
CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .failureRateThreshold(50)
    .waitDurationInOpenState(Duration.ofMillis(1000))
    .permittedNumberOfCallsInHalfOpenState(2)
    .slidingWindowSize(10)
    .build();
```

**❌ Anti-Patterns:**
```java
// Don't implement naive circuit breaker
public Response callService() {
    if (failureCount > threshold) {  // Naive implementation
        throw new ServiceUnavailableException();
    }
    return backendService.call();
}

// Avoid blocking calls in circuit breaker
CircuitBreaker.decorateCallable(circuitBreaker, () -> {
    Thread.sleep(1000);  // Blocking operation
    return backendService.call();
});
```

### Service Mesh Integration
**✅ Good Patterns:**
```java
// Health check endpoints
@GetMapping("/health")
public Health health() {
    return Health.up()
        .withDetail("heap", Runtime.getRuntime().freeMemory())
        .withDetail("threads", Thread.activeCount())
        .build();
}

// Metrics exposure
@Bean
MeterRegistry meterRegistry() {
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
}

// Distributed tracing
@Bean
public Tracer tracer() {
    return Tracing.newBuilder()
        .localServiceName("user-service")
        .spanReporter(AsyncReporter.create(sender))
        .build()
        .tracer();
}
```

**❌ Anti-Patterns:**
```java
// Avoid heavy health checks
@GetMapping("/health")
public Health health() {
    return Health.up()
        .withDetail("database", runFullDbCheck())  // Too heavy
        .withDetail("cache", checkEntireCache())   // Too heavy
        .build();
}

// Don't expose sensitive metrics
@GetMapping("/metrics")
public Map<String, Object> metrics() {
    return Map.of(
        "passwords", passwordService.getAll(),  // Security risk
        "tokens", tokenService.getActive()      // Security risk
    );
}
```

### Distributed Caching
**✅ Good Patterns:**
```java
// Redis cache with proper serialization
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(RedisSerializationContext
            .SerializationPair
            .fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext
            .SerializationPair
            .fromSerializer(new GenericJackson2JsonRedisSerializer()));
}

// Cache-aside pattern
public User getUser(String id) {
    String cacheKey = "user:" + id;
    User user = cache.get(cacheKey);
    if (user == null) {
        user = repository.findById(id);
        cache.put(cacheKey, user, Duration.ofHours(1));
    }
    return user;
}
```

**❌ Anti-Patterns:**
```java
// Don't cache everything
@Cacheable("all-data")
public List<Data> getAllData() {  // Caching too much data
    return repository.findAll();
}

// Avoid fine-grained caching
@Cacheable(value = "user-attributes", key = "#user.id + '-' + #attribute")
public String getUserAttribute(User user, String attribute) {
    return user.getAttribute(attribute);  // Too granular
}
```

## Cloud-Native Performance Patterns

### Container Optimization
**✅ Good Patterns:**
```java
// JVM container awareness
@Configuration
public class ContainerConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(
            Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(
            Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(500);
        return executor;
    }
}

// Memory limits awareness
@Bean
public CacheManager cacheManager() {
    return Caffeine.newBuilder()
        .maximumSize(
            Runtime.getRuntime().maxMemory() / (1024 * 1024 * 10))  // 10MB per entry
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();
}
```

**❌ Anti-Patterns:**
```java
// Don't use fixed thread pools in containers
ExecutorService executor = Executors.newFixedThreadPool(100);  // Ignores container limits

// Avoid fixed memory settings
@Bean
public Cache<String, byte[]> cache() {
    return Caffeine.newBuilder()
        .maximumSize(10_000_000)  // Fixed size ignores container memory
        .build();
}
```

### Kubernetes-Aware Patterns
**✅ Good Patterns:**
```java
// Graceful shutdown
@PreDestroy
public void shutdown() {
    // Stop accepting new requests
    server.stopAcceptingRequests();
    
    // Wait for ongoing requests to complete
    CompletableFuture.runAsync(() -> {
        while (activeRequests.get() > 0) {
            Thread.sleep(100);
        }
    }).get(30, TimeUnit.SECONDS);
    
    // Close resources
    closeResources();
}

// Liveness vs Readiness
@GetMapping("/health/liveness")
public Health liveness() {
    return Health.up().build();  // Basic health check
}

@GetMapping("/health/readiness")
public Health readiness() {
    return Health.up()
        .withDetail("db", dbHealthCheck())
        .withDetail("cache", cacheHealthCheck())
        .withDetail("dependencies", checkDependencies())
        .build();
}
```

**❌ Anti-Patterns:**
```java
// Don't ignore pod lifecycle
@PreDestroy
public void shutdown() {
    System.exit(0);  // Abrupt shutdown
}

// Avoid heavy readiness probes
@GetMapping("/health")
public Health health() {
    runFullSystemCheck();  // Too heavy for frequent checks
    return Health.up().build();
}
```

### Auto-Scaling Optimization
**✅ Good Patterns:**
```java
// Metrics for HPA
@Bean
public MeterRegistry meterRegistry() {
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        .config()
        .commonTags("app", "service-name")
        .meterRegistry();
}

// Custom metrics
@Scheduled(fixedRate = 15000)
public void reportMetrics() {
    Metrics.gauge("app.queue.size", 
        messageQueue.size());
    Metrics.gauge("app.active.connections", 
        activeConnections.get());
    Metrics.gauge("app.processing.time", 
        getAverageProcessingTime());
}
```

**❌ Anti-Patterns:**
```java
// Don't expose too many metrics
@Scheduled(fixedRate = 1000)
public void reportAllMetrics() {
    reportDetailedSystemMetrics();  // Too frequent and too detailed
    reportAllBusinessMetrics();     // Excessive metrics
}

// Avoid resource-heavy operations during scaling
@PostConstruct
public void init() {
    warmUpCache();  // Heavy operation during startup
    preloadAllData();  // Delays pod readiness
}
```

## Native Memory Management Patterns

### Direct Buffer Management
**✅ Good Patterns:**
```java
// Proper direct buffer allocation
public class DirectBufferPool {
    private final Queue<ByteBuffer> buffers;
    private final int bufferSize;
    
    public DirectBufferPool(int poolSize, int bufferSize) {
        this.buffers = new ConcurrentLinkedQueue<>();
        this.bufferSize = bufferSize;
        
        for (int i = 0; i < poolSize; i++) {
            buffers.offer(ByteBuffer.allocateDirect(bufferSize));
        }
    }
    
    public ByteBuffer acquire() {
        ByteBuffer buffer = buffers.poll();
        return buffer != null ? buffer : 
               ByteBuffer.allocateDirect(bufferSize);
    }
    
    public void release(ByteBuffer buffer) {
        buffer.clear();
        buffers.offer(buffer);
    }
}
```

### Large Dataset Processing
**✅ Good Patterns:**
```java
// Streaming large files
public void processLargeFile(Path path) {
    try (Stream<String> lines = Files.lines(path)) {
        lines.parallel()
             .filter(line -> line.contains("ERROR"))
             .forEach(this::processError);
    }
}

// Batch processing with cursor
@Transactional(readOnly = true)
public void processLargeDataset() {
    int batchSize = 1000;
    String lastId = "0";
    
    while (true) {
        List<Data> batch = repository
            .findByIdGreaterThanOrderById(lastId, PageRequest.of(0, batchSize));
        
        if (batch.isEmpty()) {
            break;
        }
        
        processBatch(batch);
        lastId = batch.get(batch.size() - 1).getId();
    }
}
```

## JVM Performance Patterns

### JIT Compilation Optimization
**✅ Good Patterns:**
```java
// Method size optimization
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
public void largeMethod() {  // Prevent inlining of large methods
    // Complex logic
}

// Loop optimization
public void processArray(int[] array) {
    // Help JIT with loop unrolling
    for (int i = 0; i < array.length - 4; i += 4) {
        process(array[i]);
        process(array[i + 1]); 
        process(array[i + 2]);
        process(array[i + 3]);
    }
    // Handle remaining elements
    for (int i = (array.length & ~3); i < array.length; i++) {
        process(array[i]);
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid frequent deoptimization
public void process(Object obj) {
    if (obj instanceof SpecialCase) {  // Type check causes deoptimization
        ((SpecialCase) obj).handle();
    }
    // Normal processing
}

// Avoid megamorphic call sites
public void handleObject(Object obj) {
    obj.toString();  // Many different implementations cause inline cache miss
}
```

### GC Tuning Patterns
**✅ Good Patterns:**
```java
// Object lifecycle management
public class CacheEntry {
    private WeakReference<Resource> resourceRef;
    private SoftReference<byte[]> dataRef;
    
    public Resource getResource() {
        Resource resource = resourceRef.get();
        if (resource == null) {
            resource = loadResource();
            resourceRef = new WeakReference<>(resource);
        }
        return resource;
    }
}

// Generation sizing
public static void main(String[] args) {
    // Optimize for large young generation
    System.setProperty("NewRatio", "2");  // 1/3 of heap for young gen
    System.setProperty("SurvivorRatio", "8");  // Eden:Survivor ratio
}
```

**❌ Anti-Patterns:**
```java
// Avoid allocation in critical paths
public void processBatch(List<Data> items) {
    for (Data item : items) {
        new Processor().process(item);  // Creates objects in loop
    }
}

// Avoid mixing different GC algorithms
-XX:+UseParallelGC    // Don't mix with
-XX:+UseConcMarkSweepGC  // other GC algorithms
```

### Memory Management Patterns
**✅ Good Patterns:**
```java
// Off-heap memory management
public class DirectBufferPool {
    private final Queue<ByteBuffer> buffers;
    
    public ByteBuffer acquire(int size) {
        ByteBuffer buffer = buffers.poll();
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(size);
        }
        return buffer;
    }
    
    public void release(ByteBuffer buffer) {
        buffer.clear();
        buffers.offer(buffer);
    }
}

// Stack allocation optimization
public int sumArray(int[] array) {
    int sum = 0;  // Stack allocated
    for (int value : array) {
        sum += value;
    }
    return sum;
}
```

**❌ Anti-Patterns:**
```java
// Avoid excessive temporary objects
String result = "";
for (String item : items) {
    result = result + item;  // Creates many temporary strings
}

// Avoid unnecessary boxing/unboxing
Map<Integer, Double> map = new HashMap<>();
for (int i = 0; i < 1000; i++) {
    map.put(i, (double) i);  // Unnecessary boxing
}
```

### JVM Monitoring Patterns
**✅ Good Patterns:**
```java
// JFR event recording
@Label("Custom Business Event")
@Description("Records business operation details")
@Category("Business")
public class BusinessEvent extends jdk.jfr.Event {
    @Label("Operation Name")
    private String operation;
    
    @Label("Duration")
    private long duration;
    
    public void record(String op, long time) {
        this.operation = op;
        this.duration = time;
        commit();
    }
}

// MBean monitoring
@MBean
public class PerformanceMonitor implements PerformanceMonitorMBean {
    private final AtomicLong operationCount = new AtomicLong();
    
    public long getOperationCount() {
        return operationCount.get();
    }
    
    public void recordOperation() {
        operationCount.incrementAndGet();
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid excessive monitoring overhead
public void process() {
    long start = System.nanoTime();  // Too fine-grained timing
    // Process
    logger.debug("Operation took: " + (System.nanoTime() - start));
}

// Avoid JMX without security
MBeanServer server = ManagementFactory.getPlatformMBeanServer();
server.registerMBean(monitor, name);  // No security configuration
```

### JVM Thread Management Patterns
**✅ Good Patterns:**
```java
// Thread pool sizing based on CPU cores
public class OptimalThreadPool {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    
    public ExecutorService createCpuBoundPool() {
        return new ThreadPoolExecutor(
            CORE_COUNT,                    // Core pool size
            CORE_COUNT,                    // Max pool size
            60L, TimeUnit.SECONDS,         // Keep-alive time
            new LinkedBlockingQueue<>(1000),// Queue capacity
            new ThreadFactoryBuilder()
                .setNameFormat("cpu-pool-%d")
                .build()
        );
    }
    
    public ExecutorService createIoBoundPool() {
        return new ThreadPoolExecutor(
            CORE_COUNT * 2,               // More threads for I/O
            CORE_COUNT * 4,               // Max threads
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder()
                .setNameFormat("io-pool-%d")
                .build()
        );
    }
}

// Thread affinity for critical operations
public class AffinityExample {
    static {
        System.loadLibrary("affinity");  // Native library
    }
    
    private native void setThreadAffinity(int cpuId);
    
    public void runCriticalTask() {
        setThreadAffinity(0);  // Pin to CPU 0
        // Critical operation
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid thread pool starvation
public class ThreadPoolStarvation {
    ExecutorService executor = Executors.newFixedThreadPool(2);  // Too few threads
    
    public void processRequests() {
        for (Request req : requests) {
            executor.submit(() -> {
                Thread.sleep(1000);  // Long blocking operation
                process(req);
            });
        }
    }
}

// Avoid thread local memory leaks
public class ThreadLocalLeak {
    private static ThreadLocal<byte[]> buffer =
        ThreadLocal.withInitial(() -> new byte[1024 * 1024]);
    
    public void process() {
        byte[] data = buffer.get();
        // Use buffer but never clean it
        // ThreadLocal not removed when thread dies
    }
}
```

### JVM Code Cache Optimization
**✅ Good Patterns:**
```java
// Code cache sizing
public class CodeCacheConfig {
    public static void configureCodeCache() {
        // Reserve space for JIT compiled code
        System.setProperty("ReservedCodeCacheSize", "240m");
        System.setProperty("InitialCodeCacheSize", "160m");
    }
}

// Method profiling for JIT
public class JitOptimization {
    @CompilerControl(CompilerControl.Mode.PRINT)  // Print compilation details
    public void hotMethod() {
        // Frequently called method
    }
    
    @CompilerControl(CompilerControl.Mode.EXCLUDE)  // Prevent compilation
    public void rarePath() {
        // Rarely executed code
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid code cache fragmentation
public class CodeCacheFragmentation {
    public void generateManyMethods() {
        for (int i = 0; i < 1000; i++) {
            // Dynamic class generation
            generateNewClass("Class" + i);  // Fills code cache
        }
    }
}

// Avoid excessive inlining
public class ExcessiveInlining {
    public void smallMethod1() { /* tiny method */ }
    public void smallMethod2() { /* tiny method */ }
    
    public void caller() {
        for (int i = 0; i < 1000000; i++) {
            smallMethod1();  // Forces aggressive inlining
            smallMethod2();  // May overflow inline cache
        }
    }
}
```

### JVM Native Memory Patterns
**✅ Good Patterns:**
```java
// Native memory tracking
public class NativeMemoryTracker {
    public static void enableNMT() {
        // Enable detailed native memory tracking
        System.setProperty("NativeMemoryTracking", "detail");
        
        // Log native memory usage periodically
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            ProcessBuilder pb = new ProcessBuilder(
                "jcmd", getPid(), "VM.native_memory", "summary"
            );
            // Execute and log output
        }, 0, 1, TimeUnit.HOURS);
    }
}

// Direct buffer management
public class SafeDirectBufferPool {
    private static final long MAX_DIRECT_MEMORY = 
        Runtime.getRuntime().maxMemory() / 4;  // 25% of heap
    private final AtomicLong currentDirectMemory = new AtomicLong(0);
    
    public ByteBuffer allocateDirectBuffer(int size) {
        if (currentDirectMemory.get() + size > MAX_DIRECT_MEMORY) {
            System.gc();  // Hint for cleaning direct buffers
            throw new OutOfMemoryError("Direct buffer limit exceeded");
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        currentDirectMemory.addAndGet(size);
        return buffer;
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid unchecked native memory growth
public class NativeMemoryLeak {
    private List<ByteBuffer> directBuffers = new ArrayList<>();
    
    public void loadData() {
        while (true) {
            directBuffers.add(
                ByteBuffer.allocateDirect(1024 * 1024)  // Never released
            );
        }
    }
}

// Avoid excessive JNI calls
public class ExcessiveJNI {
    private native byte[] processDataNative(byte[] data);
    
    public void processStream(InputStream is) {
        byte[] buffer = new byte[1024];
        while (is.read(buffer) != -1) {
            processDataNative(buffer);  // Frequent JNI transitions
        }
    }
}
```

### JVM Compiler Optimization
**✅ Good Patterns:**
```java
// Tiered compilation settings
public class CompilerOptimization {
    public static void configureTiered() {
        // Enable tiered compilation
        System.setProperty("TieredCompilation", "true");
        System.setProperty("TieredStopAtLevel", "4");
        
        // Configure compilation thresholds
        System.setProperty("Tier3InvocationThreshold", "2000");
        System.setProperty("Tier4InvocationThreshold", "15000");
    }
}

// Profile-guided optimization
public class PGOExample {
    static {
        // Use profile data from previous runs
        System.setProperty("UseProfiledJVMArgs", "true");
        System.setProperty("ProfiledCodeCacheSize", "300m");
    }
    
    @Profile  // Custom annotation for profiling
    public void hotPath() {
        // Critical business logic
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid disabling JIT
public class JITDisabled {
    static {
        System.setProperty("java.compiler", "NONE");  // Don't disable JIT
    }
}

// Avoid excessive method sizes
public class HugeMethod {
    public void doEverything() {  // Method too large for JIT
        // Hundreds of lines of code
        // Makes inlining impossible
        // Harder for JIT to optimize
    }
}
```

### JVM Ergonomics Patterns
**✅ Good Patterns:**
```java
// Container-aware settings
public class ContainerConfig {
    public static void optimizeForContainer() {
        // Use container CPU limits
        System.setProperty("UseContainerSupport", "true");
        
        // Optimize heap for container
        System.setProperty("MaxRAMPercentage", "75.0");
        System.setProperty("InitialRAMPercentage", "50.0");
        
        // Thread pool sizing
        int cpuLimit = Runtime.getRuntime().availableProcessors();
        System.setProperty("ParallelGCThreads", 
            String.valueOf(Math.max(2, cpuLimit/2)));
    }
}

// Adaptive sizing
public class AdaptiveSizing {
    public static void enableAdaptive() {
        // Enable adaptive sizing
        System.setProperty("UseAdaptiveSizePolicy", "true");
        System.setProperty("AdaptiveSizePolicyWeight", "90");
        
        // Adaptive GC
        System.setProperty("UseAdaptiveGCBoundary", "true");
        System.setProperty("GCTimeRatio", "19");
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid fixed sizing in containers
public class FixedSizing {
    static {
        System.setProperty("Xmx4g");  // Don't use fixed sizes
        System.setProperty("Xms4g");  // in containers
    }
}

// Avoid ignoring container limits
public class ContainerIgnored {
    public void configureThreads() {
        int threads = 100;  // Ignoring container CPU limits
        executor = Executors.newFixedThreadPool(threads);
    }
}
```

### JVM Profiling and Diagnostics
**✅ Good Patterns:**
```java
// JFR configuration
public class JFRConfig {
    public static void startProfiling() {
        // Configure JFR
        String settings = "settings=profile,disk=true," +
                         "dumponexit=true,duration=2h";
        System.setProperty("StartFlightRecording", settings);
        
        // Add custom events
        JFR.register(CustomEvent.class);
    }
}

// Async-profiler integration
public class AsyncProfiler {
    public static void profileAllocation() {
        // Profile allocations
        ProcessBuilder pb = new ProcessBuilder(
            "async-profiler.sh", "-d", "30", "-e", "alloc", 
            String.valueOf(ProcessHandle.current().pid())
        );
        pb.start();
    }
}
```

**❌ Anti-Patterns:**
```java
// Avoid production profiling overhead
public class HeavyProfiling {
    public void profile() {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        tmx.setThreadCpuTimeEnabled(true);  // High overhead
        tmx.setThreadContentionMonitoringEnabled(true);
    }
}

// Avoid excessive sampling
public class ExcessiveSampling {
    public void startProfiler() {
        // Too frequent sampling
        System.setProperty("FlightRecorderOptions",
            "stackdepth=64,sample-threads=true,period=1ms");
    }
}
``` 