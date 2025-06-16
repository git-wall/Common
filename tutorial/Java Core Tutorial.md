# Java Core Tutorial

## Table of Contents
1. [Java Fundamentals](#1-java-fundamentals)
2. [Object-Oriented Programming](#2-object-oriented-programming)
3. [Memory Management](#3-memory-management)
4. [Advanced Features](#4-advanced-features)
5. [Modern Java Features](#5-modern-java-features)
6. [Common Use Cases](#6-common-use-cases)
## 1. Java Fundamentals

### JDK, JRE, and JVM
**What:**
- Development and runtime environment for Java applications
- Platform independence ("Write Once, Run Anywhere")

**How:**
```java
// 1. Write Java code (HelloWorld.java)
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}

// 2. Compile with JDK: javac HelloWorld.java
// 3. Run with JRE: java HelloWorld
```

**When:**
- JDK: During development
- JRE: For running Java applications
- JVM: Automatically manages execution

**Where:**
- Development environments
- Production servers
- Client machines running Java applications

### Data Types
**What:**
- Primitive types: Basic data types
- Reference types: Object references

**How:**
```java
// Primitive Types
int age = 25;                  // 32-bit integer
double salary = 50000.0;       // 64-bit floating-point
boolean isActive = true;       // true/false
char grade = 'A';              // 16-bit Unicode character

// Reference Types
String name = "John";          // String object
Date birthDate = new Date();   // Date object
int[] numbers = {1, 2, 3};     // Array reference

// `null` is 'memory' address with all bits set to 0 (address 0x0)
// This address is special and protected by the programs aren't allowed to actually access this memory location.
String x; // Reference variable created
x: [0x0]  // Contains the null address (typically 0x0)
x = "Hello";
x: [0x1234]  // Contains address of the String object
             // (e.g., 0x1234 points to where "Hello" is stored)
```

**When:**
- Primitives: For simple values and better performance
- Reference types: For complex objects and collections

**Where:**
- Variables and fields
- Method parameters and return types
- Collections and arrays

### Control Flow
**What:**
- Program execution control
- Decision making and loops

**How:**
```java
// Decision Making
if (condition) {
    // code block
} else if (anotherCondition) {
    // code block
} else {
    // code block
}

// Modern Switch (Java 17+)
String result = switch (value) {
    case 1 -> "One";
    case 2 -> "Two";
    default -> "Other";
};

// Loops
for (int i = 0; i < 5; i++) {
    if (i == 2) continue;  // Skip iteration
    if (i == 4) break;     // Exit loop
    System.out.println(i);
}

// Enhanced for loop
for (String item : items) {
    System.out.println(item);
}
```

**When:**
- Conditional execution
- Repetitive tasks
- Collection iteration

**Where:**
- Business logic
- Data processing
- User input handling

## 2. Object-Oriented Programming

### Classes and Objects
**What:**
- Blueprint for creating objects
- Encapsulation of data and behavior

**How:**
```java
public class User {
    // Fields (attributes)
    private String name;
    private String email;
    
    // Constructor
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    // Methods (behavior)
    public void sendEmail(String message) {
        System.out.println("Sending email to " + email);
        // Email sending logic
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// Usage
User user = new User("John", "john@example.com");
user.sendEmail("Hello!");
```

**When:**
- Modeling real-world entities
- Organizing code logically
- Creating reusable components

**Where:**
- Business objects
- Service classes
- Data models

### Inheritance and Polymorphism
**What:**
- Code reuse through inheritance
- Runtime behavior through polymorphism

**How:**
```java
// Base class
public abstract class Account {
    protected double balance;
    
    public abstract void withdraw(double amount);
    
    public void deposit(double amount) {
        balance += amount;
    }
}

// Derived class
public class SavingsAccount extends Account {
    private double interestRate;
    
    @Override
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
        }
    }
    
    public void addInterest() {
        balance += balance * interestRate;
    }
}

// Polymorphism
Account account = new SavingsAccount();
account.withdraw(100);  // Calls SavingsAccount's withdraw
```

**When:**
- Creating class hierarchies
- Implementing common interfaces
- Extending existing functionality

**Where:**
- Framework design
- Plugin systems
- Generic algorithms

## 3. Memory Management

### Stack vs Heap
**What:**
- Stack: Method calls and local variables
- Heap: Object storage

**How:**
```java
public void memoryExample() {
    int x = 10;                    // Stack
    String name = "John";          // Stack reference, Heap object
    User user = new User(name);    // Stack reference, Heap object
    
    // Method ends: stack variables cleared automatically
    // Heap objects eligible for GC when no references exist
}
```

**When:**
- Stack: Method-scope variables
- Heap: Objects and long-lived data

**Where:**
- JVM memory management
- Performance optimization
- Resource management

### Garbage Collection
**What:**
- Automatic memory management
- Object lifecycle management

**How:**
```java
public class ResourceManager {
    private List<Resource> resources = new ArrayList<>();
    
    public void processResource() {
        Resource temp = new Resource();  // Created in heap
        resources.add(temp);             // Referenced by list
        
        temp = null;                     // Original reference removed
        resources.clear();               // All resources eligible for GC
    }
}
```

**When:**
- Objects no longer referenced
- Memory pressure
- Application shutdown

**Where:**
- JVM heap management
- Resource cleanup
- Memory leak prevention

## 4. Advanced Features

### Collections Framework
**What:**
- Data structure implementations
- Collection manipulation

**How:**
```java
// List - Ordered collection
List<String> list = new ArrayList<>();
list.add("First");
list.add("Second");

// Set - Unique elements
Set<Integer> set = new HashSet<>();
set.add(1);
set.add(2);
set.add(1);  // Ignored (duplicate)

// Map - Key-value pairs
Map<String, Integer> map = new HashMap<>();
map.put("One", 1);
map.put("Two", 2);

// Queue - FIFO
Queue<String> queue = new LinkedList<>();
queue.offer("First");
String first = queue.poll();
```

**When:**
- Data storage and retrieval
- Algorithms implementation
- Data processing

**Where:**
- Data management
- Caching
- Algorithm implementation

### Multithreading
**What:**
- Concurrent execution
- Parallel processing

**How:**
```java
// Thread creation
class Worker extends Thread {
    public void run() {
        System.out.println("Worker running");
    }
}

// Synchronization
public class Counter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
}

// Modern concurrency
ExecutorService executor = Executors.newFixedThreadPool(4);
Future<String> future = executor.submit(() -> {
    // Long running task
    return "Result";
});
```

**When:**
- Parallel processing
- UI responsiveness
- Background tasks

**Where:**
- Server applications
- Desktop applications
- Data processing

### Stream API
**What:**
- Functional-style operations
- Data processing pipeline

**How:**
```java
List<User> users = getUsers();

// Filter and map
List<String> activeEmails = users.stream()
    .filter(User::isActive)
    .map(User::getEmail)
    .collect(Collectors.toList());

// Parallel processing
double avgAge = users.parallelStream()
    .mapToInt(User::getAge)
    .average()
    .orElse(0);
```

**When:**
- Data transformation
- Filtering
- Aggregation

**Where:**
- Data processing
- Report generation
- ETL operations

### Processing Input and Output
**What:**
- File and stream handling
- NIO.2 (New I/O) API
- Buffered operations

**How:**
```java
// File operations with NIO.2
Path path = Paths.get("data.txt");
List<String> lines = Files.readAllLines(path);

// Buffered I/O
try (BufferedReader reader = Files.newBufferedReader(path)) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}

// File walking and searching
Files.walk(Paths.get("."))
    .filter(Files::isRegularFile)
    .filter(p -> p.toString().endsWith(".java"))
    .forEach(System.out::println);

// Async I/O
AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path);
ByteBuffer buffer = ByteBuffer.allocate(1024);
fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        // Handle completed read
    }
    
    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        // Handle failed read
    }
});
```

**When:**
- File processing
- Network communication
- Data persistence
- Resource management

**Where:**
- File operations
- Network applications
- Data import/export
- Logging systems

### Concurrent Programming
**What:**
- Thread management
- Synchronization
- Parallel execution
- Thread safety

**How:**
```java
// CompletableFuture
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Step 1")
    .thenApply(result -> result + " -> Step 2")
    .thenApply(result -> result + " -> Step 3");

// Parallel Stream
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
int sum = numbers.parallelStream()
                .mapToInt(i -> i * 2)
                .sum();

// Thread Pool
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(() -> {
    // Task logic
});

// Lock API
ReentrantLock lock = new ReentrantLock();
try {
    lock.lock();
    // Critical section
} finally {
    lock.unlock();
}

// Atomic Operations
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();
```

**When:**
- High-performance applications
- Responsive UIs
- Parallel processing
- Resource sharing

**Where:**
- Server applications
- Real-time systems
- Data processing
- GUI applications

### Annotations
**What:**
- Metadata about code
- Compile-time checks
- Runtime processing

**How:**
```java
// Custom annotation definition
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Metrics {
    String value() default "";
    boolean enabled() default true;
}

// Using annotations
@Metrics("userAccess")
public class UserService {
    @Deprecated
    @Metrics(value = "legacy", enabled = false)
    public void oldMethod() {
        // Method implementation
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        // Method implementation
    }
}

// Processing annotations
Method[] methods = UserService.class.getDeclaredMethods();
for (Method method : methods) {
    if (method.isAnnotationPresent(Metrics.class)) {
        Metrics metrics = method.getAnnotation(Metrics.class);
        // Process annotation
    }
}
```

**When:**
- Configuration
- Documentation
- Validation
- Code generation

**Where:**
- Framework development
- API design
- Testing
- Documentation

### The Date and Time API
**What:**
- Modern date/time handling
- Time zone support
- Duration and period calculations

**How:**
```java
// Current date/time
LocalDateTime now = LocalDateTime.now();
ZonedDateTime zonedNow = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

// Date/time manipulation
LocalDate tomorrow = LocalDate.now().plusDays(1);
LocalTime afterHour = LocalTime.now().plusHours(1);

// Periods and durations
Period period = Period.between(LocalDate.now(), LocalDate.now().plusYears(1));
Duration duration = Duration.between(Instant.now(), Instant.now().plusSeconds(3600));

// Formatting
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
String formatted = LocalDateTime.now().format(formatter);

// Parsing
LocalDate parsed = LocalDate.parse("2024-03-15");
```

**When:**
- Time-based operations
- Scheduling
- Event management
- Time zone handling

**Where:**
- Business applications
- Scheduling systems
- International applications
- Log management

### Internationalization
**What:**
- Multi-language support
- Locale-specific formatting
- Resource bundles

**How:**
```java
// Resource bundle
ResourceBundle bundle = ResourceBundle.getBundle("messages", 
    new Locale("fr", "FR"));
String message = bundle.getString("welcome");

// Number formatting
NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
String formatted = numberFormat.format(1234.56);

// Date formatting
DateFormat dateFormat = DateFormat.getDateInstance(
    DateFormat.LONG, Locale.JAPANESE);
String date = dateFormat.format(new Date());

// Currency
NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(
    new Locale("de", "DE"));
String price = currencyFormat.format(49.99);
```

**When:**
- Global applications
- Multi-language support
- Regional formatting
- Cultural adaptation

**Where:**
- International applications
- E-commerce systems
- User interfaces
- Documentation

### The Java Platform Module System
**What:**
- Code organization
- Encapsulation
- Dependency management

**How:**
```java
// module-info.java
module com.example.myapp {
    requires java.base;
    requires java.sql;
    
    exports com.example.myapp.api;
    provides com.example.myapp.spi.Service with
        com.example.myapp.internal.ServiceImpl;
}

// Using modules
import com.example.myapp.api.MyService;

// Module compilation
javac -d mods/com.example.myapp 
    src/com.example.myapp/module-info.java 
    src/com.example.myapp/com/example/myapp/**/*.java

// Running modular application
java --module-path mods -m com.example.myapp/com.example.myapp.Main
```

**When:**
- Large applications
- Framework development
- Platform development
- Security requirements

**Where:**
- Enterprise applications
- Libraries
- Frameworks
- Platform development

### Compiling and Scripting
**What:**
- Dynamic code execution
- Script evaluation
- Runtime compilation

**How:**
```java
// Java compiler API
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
int result = compiler.run(null, null, null, "Source.java");

// Script engine
ScriptEngineManager manager = new ScriptEngineManager();
ScriptEngine engine = manager.getEngineByName("nashorn");
Object result = engine.eval("'Hello, ' + name");

// Dynamic compilation
String source = "public class Dynamic { public void run() { } }";
CompilationTask task = compiler.getTask(null, fileManager, null,
    null, null, Arrays.asList(new StringJavaFileObject(source)));
boolean success = task.call();
```

**When:**
- Dynamic code execution
- Plugin systems
- Rule engines
- Testing frameworks

**Where:**
- Development tools
- Testing frameworks
- Dynamic systems
- Code generators

## 6. Common Use Cases

### Web Applications
**What:**
- HTTP-based applications
- RESTful services

**How:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping
    public List<User> getUsers() {
        return userService.findAll();
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

**When:**
- Web services
- APIs
- Web applications

**Where:**
- Enterprise applications
- Microservices
- Web platforms

### Enterprise Applications
**What:**
- Business applications
- Large-scale systems

**How:**
```java
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    public Order createOrder(Order order) {
        validateOrder(order);
        calculateTotal(order);
        return orderRepository.save(order);
    }
    
    @Cacheable("orders")
    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException());
    }
}
```

**When:**
- Business operations
- Data processing
- Integration

**Where:**
- Corporate systems
- Business applications
- Service platforms

## Performance Best Practices

### JVM Memory Management
**What:**
- Understanding heap vs stack
- Garbage collection mechanics
- Memory leaks prevention

**How:**
```java
// Stack vs Heap example
public void memoryExample() {
    int x = 5;                     // Stack
    String str = "Hello";          // String pool (Heap)
    Object obj = new Object();     // Heap
    
    // Memory-efficient string handling
    StringBuilder builder = new StringBuilder(50);  // Pre-sized buffer
    for(int i = 0; i < 50; i++) {
        builder.append(i);         // No new objects created
    }
}

// Proper resource management
public class ResourceHandler implements AutoCloseable {
    private final ByteBuffer buffer;
    
    public ResourceHandler() {
        this.buffer = ByteBuffer.allocateDirect(1024);
    }
    
    @Override
    public void close() {
        // Clean up direct buffer
        if (buffer != null) {
            ((DirectBuffer) buffer).cleaner().clean();
        }
    }
}
```

## Java SE 8 Core Concepts

### Class Structure
```java
// Basic class structure
public class MyClass {
    // Fields (instance variables)
    private int value;
    
    // Constructor
    public MyClass(int value) {
        this.value = value;
    }
    
    // Methods
    public void doSomething() {
        // Method implementation
    }
}
```

### Package Organization
```java
// Package declaration must be first non-comment line
package com.example.myapp;

// Import statements come after package
import java.util.List;
import java.util.ArrayList;

// Class declaration follows
public class MyClass {
    // Class implementation
}
```

### Access Modifiers
- **public**: Accessible from any class
- **protected**: Accessible in same package and subclasses
- **default (package-private)**: Accessible only in same package
- **private**: Accessible only in same class

### Variable Scoping
```java
public class ScopeExample {
    private int instanceVar;  // Instance scope
    private static int classVar;  // Class scope
    
    public void method() {
        int localVar = 0;  // Method scope
        for (int i = 0; i < 10; i++) {  // Block scope
            // i is only accessible here
        }
    }
}
``` 