# Spring Boot Tutorial

## 1. Auto-configuration

### What
- Automatic configuration of Spring application based on dependencies
- Smart defaults with override capability
- Conditional bean creation

### How
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Custom auto-configuration
@Configuration
@ConditionalOnClass(DataSource.class)
public class DatabaseConfig {
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
}
```

### Where
- Entry point of Spring Boot applications
- Configuration classes
- Custom auto-configuration modules

### When
- Starting a new Spring Boot project
- Need for custom configuration behavior
- Overriding default configurations

## 2. Properties Configuration

### What
- External configuration for applications
- Environment-specific settings
- Type-safe configuration properties

### How
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
  jpa:
    hibernate:
      ddl-auto: update

# Type-safe configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    @NotNull
    private String apiKey;
    private int cacheTimeout = 3600;
    // getters and setters
}
```

### Where
- Resource directory (application.properties/yml)
- Environment-specific files
- External configuration sources

### When
- Configuring application settings
- Environment-specific configurations
- Externalizing configuration

## 3. Web Layer

### What
- REST API endpoints
- Request/Response handling
- Web MVC configuration

### How
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(userService.create(userDTO));
    }
}
```

### Where
- Web layer of the application
- REST API endpoints
- Controller classes

### When
- Building REST APIs
- Handling HTTP requests
- Web application development

## 4. Data Access

### What
- Database interactions
- ORM configuration
- Repository pattern implementation

### How
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Email
    private String email;
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> searchByName(@Param("name") String name);
}
```

### Where
- Data access layer
- Repository interfaces 
- Entity classes

### When
- Database operations
- Data persistence
- Entity relationship mapping

## 5. Service Layer

### What
- Business logic implementation
- Transaction management
- Service orchestration

### How
```java
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public User create(UserDTO dto) {
        validateUserDTO(dto);
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return userRepository.save(user);
    }
}
```

### Where
- Service layer classes
- Business logic components
- Transaction boundaries

### When
- Implementing business rules
- Coordinating multiple operations
- Managing transactions

## 6. Security

### What
- Authentication and authorization
- Security configurations
- JWT implementation

### How
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login()
            .and()
            .authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
```

### Where
- Security configuration
- Authentication providers
- Authorization rules

### When
- Securing applications
- Implementing authentication
- Managing authorization

## 7. Testing

### What
- Unit testing
- Integration testing
- Test configurations

### How
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void shouldCreateUser() throws Exception {
        UserDTO dto = new UserDTO("Test User", "test@example.com");
        when(userService.create(any(UserDTO.class)))
            .thenReturn(new User(1L, "Test User", "test@example.com"));
            
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"));
    }
}
```

### Where
- Test classes
- Test configurations
- Test resources

### When
- Writing unit tests
- Integration testing
- API testing

## 8. Monitoring

### What
- Application health monitoring
- Metrics collection
- Logging configuration

### How
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.example: DEBUG
```

### Where
- Actuator endpoints
- Monitoring configurations
- Logging setup

### When
- Production monitoring
- Application health checks
- Performance monitoring

## 9. Caching

### What
- Application-level caching
- Cache abstraction
- Distributed caching

### How
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("users"),
            new ConcurrentMapCache("roles")
        ));
        return cacheManager;
    }
}

@Service
public class UserService {
    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        // Database lookup
        return userRepository.findById(id).orElseThrow();
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void updateUser(User user) {
        userRepository.save(user);
    }
}
```

### Where
- Service layer methods
- Repository methods
- Resource-intensive operations

### When
- Performance optimization
- Reducing database load
- High-traffic applications

## 10. Messaging

### What
- Asynchronous communication
- Message queues
- Event handling

### How
```java
@Configuration
public class MessagingConfig {
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }
}

@Service
public class MessageService {
    @Autowired
    private JmsTemplate jmsTemplate;
    
    public void sendMessage(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
    }
    
    @JmsListener(destination = "myQueue")
    public void receiveMessage(String message) {
        // Process message
        logger.info("Received message: {}", message);
    }
}
```

### Where
- Messaging services
- Event handlers
- Integration points

### When
- Decoupled communication
- Event-driven architecture
- Async processing

## 11. Validation

### What
- Input validation
- Business rule validation
- Cross-field validation

### How
```java
@Data
public class UserDTO {
    @NotNull
    @Size(min = 2, max = 50)
    private String name;
    
    @Email
    @NotEmpty
    private String email;
    
    @Pattern(regexp = "^\\d{10}$")
    private String phone;
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO, 
                                         BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
        return ResponseEntity.ok(userService.create(userDTO));
    }
}
```

### Where
- DTOs and domain models
- Controllers
- Service layer validation

### When
- Input validation
- Form handling
- API requests

## 12. Exception Handling

### What
- Global exception handling
- Custom exceptions
- Error responses

### How
```java
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            ex.getErrors()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
```

### Where
- Global exception handlers
- Controller-specific handlers
- Custom error pages

### When
- Error handling
- API error responses
- User feedback

## 13. Internationalization (i18n)

### What
- Multi-language support
- Locale-based content
- Message externalization

### How
```java
@Configuration
public class I18nConfig implements WebMvcConfigurer {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
}

// messages.properties
user.welcome=Welcome, {0}!
user.email.invalid=Invalid email address

// messages_es.properties
user.welcome=¡Bienvenido, {0}!
user.email.invalid=Dirección de correo inválida
```

### Where
- Message resources
- View templates
- Error messages

### When
- Multi-language applications
- International user base
- Localized content

## 14. WebSocket Support

### What
- Real-time communication
- Bi-directional messaging
- Server push capabilities

### How
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/chat")
               .setAllowedOrigins("*");
    }
}

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, 
                                   TextMessage message) throws Exception {
        // Handle incoming message
        String payload = message.getPayload();
        session.sendMessage(new TextMessage("Received: " + payload));
    }
}
```

### Where
- Real-time features
- Chat applications
- Live updates

### When
- Real-time communication needs
- Interactive applications
- Live data streaming

## 15. File Handling

### What
- File upload/download
- Multipart requests
- File storage

### How
```java
@RestController
@RequestMapping("/api/files")
public class FileController {
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String filename = storageService.store(file);
            return ResponseEntity.ok("Uploaded: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Failed to upload");
        }
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
```

### Where
- File upload endpoints
- Document management
- Media handling

### When
- Document processing
- Media uploads
- File downloads

## 16. Scheduling

### What
- Task scheduling
- Cron jobs
- Periodic execution

### How
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void reportCurrentTime() {
        // Task execution
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void hourlyTask() {
        // Hourly task
    }
}

@Service
public class EmailService {
    @Async
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void sendDailyReport() {
        // Generate and send report
    }
}
```

### Where
- Background tasks
- Maintenance jobs
- Report generation

### When
- Periodic tasks
- Background processing
- Automated jobs

## 17. Batch Processing

### What
- Large-scale data processing
- Batch job management
- ETL operations

### How
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Bean
    public Job importUserJob(JobBuilderFactory jobs, Step step1) {
        return jobs.get("importUserJob")
                  .incrementer(new RunIdIncrementer())
                  .flow(step1)
                  .end()
                  .build();
    }
    
    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, 
                     ItemReader<User> reader,
                     ItemProcessor<User, User> processor,
                     ItemWriter<User> writer) {
        return stepBuilderFactory.get("step1")
                .<User, User>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

### Where
- Data migration jobs
- Report generation
- Data processing pipelines

### When
- Large dataset processing
- Scheduled data imports
- Data transformation tasks

## 18. Actuator Customization

### What
- Custom health indicators
- Metrics endpoints
- Application information

### How
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // Check custom health logic
            return Health.up()
                        .withDetail("customService", "running")
                        .build();
        } catch (Exception e) {
            return Health.down()
                        .withException(e)
                        .build();
        }
    }
}

@Component
public class CustomEndpoint {
    @Endpoint(id = "custom")
    public CustomMetrics customMetrics() {
        return new CustomMetrics(
            // Custom metrics data
        );
    }
}
```

### Where
- Health check implementations
- Custom metrics endpoints
- Monitoring configurations

### When
- Custom health monitoring
- Application-specific metrics
- Enhanced monitoring needs

## 19. Spring Boot Testing Advanced

### What
- Test slices
- Custom test configurations
- Performance testing

### How
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindByEmail() {
        // Test repository methods
    }
}

@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockBean
    private UserService userService;
    
    @Test
    void shouldReturnUserNotFound() {
        // Test controller behavior
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerformanceTest {
    @Test
    void shouldHandleConcurrentRequests() {
        // Performance test implementation
    }
}
```

### Where
- Test packages
- CI/CD pipelines
- Performance test suites

### When
- Testing specific layers
- Performance validation
- Integration testing

## 20. Production Deployment

### What
- Production configuration
- Container deployment
- Cloud platform setup

### How
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  cache:
    type: redis
  security:
    require-ssl: true

# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Where
- Production environments
- Cloud platforms
- Container orchestration

### When
- Application deployment
- Production release
- Scaling operations

[End of Tutorial]
