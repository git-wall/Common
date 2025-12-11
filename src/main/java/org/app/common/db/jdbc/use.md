# Hướng dẫn sử dụng BaseRepository

## 📋 Giới thiệu

BaseRepository là một ORM đơn giản nhưng mạnh mẽ, kết hợp tốc độ của JDBC với sự tiện lợi của JPA. Framework này cho phép bạn:

- ✅ Mapping tự động giữa Java object và database
- ✅ Type-safe và thread-safe
- ✅ Hỗ trợ batch operations hiệu suất cao
- ✅ Pagination tích hợp sẵn
- ✅ Không cần viết RowMapper thủ công

## 🚀 Bước 1: Cấu hình Entity

### 1.1. Tạo Entity Class

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String name;
    private Integer age;
    private LocalDateTime createdAt;
}
```

### 1.2. Đăng ký Metadata

Tạo một Configuration class để đăng ký metadata cho entity:

```java
@Configuration
public class EntityMetadataConfig {
    
    @PostConstruct
    public void registerUserMetadata() {
        // 1. Đăng ký constructor
      EntityMetadata.registerNewInstance(User.class, User::new);
        
        // 2. Đăng ký table name
      EntityMetadata.registerTableName(User.class, "users");
        
        // 3. Đăng ký column mappings
      EntityMetadata.registerColumnMappings(User.class, List.of(
            ColumnMapping.of("id", User::getId, User::setId),
            ColumnMapping.of("email", User::getEmail, User::setEmail),
            ColumnMapping.of("name", User::getName, User::setName),
            ColumnMapping.of("age", User::getAge, User::setAge),
            ColumnMapping.of("created_at", User::getCreatedAt, User::setCreatedAt)
        ));
    }
}
```

## 💾 Bước 2: Tạo Repository

### 2.1. Repository Class

```java
@Repository
@RequiredArgsConstructor
public class UserRepository {
    
    private final BaseRepository baseRepository;
    
    // INSERT
    public void create(User user) {
        String sql = """
            INSERT INTO users (email, name, age, created_at)
            VALUES (:email, :name, :age, :createdAt)
            """;
        
        Map<String, Object> params = EntityMetadata.toParamMap(User.class, user);
        baseRepository.insert(sql, params);
    }
    
    // INSERT với RETURNING ID
    public Long createAndGetId(User user) {
        String sql = """
            INSERT INTO users (email, name, age, created_at)
            VALUES (:email, :name, :age, :createdAt)
            RETURNING id
            """;
        
        Map<String, Object> params = EntityMetadata.toParamMap(User.class, user);
        return baseRepository.insertReturningId(sql, params).longValue();
    }
    
    // SELECT ALL
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return baseRepository.select(sql, Map.of(), User.class);
    }
    
    // SELECT BY ID
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = :id";
        return baseRepository.findOne(sql, Map.of("id", id), User.class);
    }
    
    // SELECT BY EMAIL
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = :email";
        return baseRepository.findOne(sql, Map.of("email", email), User.class);
    }
    
    // UPDATE
    public int update(User user) {
        String sql = """
            UPDATE users 
            SET email = :email, name = :name, age = :age
            WHERE id = :id
            """;
        
        Map<String, Object> params = EntityMetadata.toParamMap(User.class, user);
        return baseRepository.update(sql, params);
    }
    
    // DELETE
    public int delete(Long id) {
        String sql = "DELETE FROM users WHERE id = :id";
        return baseRepository.delete(sql, Map.of("id", id));
    }
    
    // EXISTS
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";
        return baseRepository.exists(sql, Map.of("email", email));
    }
    
    // COUNT
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        return baseRepository.count(sql, Map.of());
    }
}
```

## 📄 Bước 3: Pagination

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final BaseRepository baseRepository;
    
    public Page<User> getUsers(int page, int size) {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        return baseRepository.selectPage(
            sql, 
            Map.of(), 
            User.class, 
            page,  // page number (bắt đầu từ 0)
            size   // page size
        );
    }
    
    public Page<User> searchByName(String name, int page, int size) {
        String sql = """
            SELECT * FROM users 
            WHERE name ILIKE :name 
            ORDER BY created_at DESC
            """;
        
        return baseRepository.selectPage(
            sql,
            Map.of("name", "%" + name + "%"),
            User.class,
            page,
            size
        );
    }
}
```

### Sử dụng Page Object

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<PageResponse<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<User> userPage = userService.getUsers(page, size);
        
        return ResponseEntity.ok(new PageResponse<>(
            userPage.getContent(),
            userPage.getPageNumber(),
            userPage.getPageSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.hasNext(),
            userPage.hasPrevious()
        ));
    }
}
```

## ⚡ Bước 4: Batch Operations

### 4.1. Batch Insert

```java
public void createBatch(List<User> users) {
    String sql = """
        INSERT INTO users (email, name, age, created_at)
        VALUES (?, ?, ?, ?)
        """;
    
    // Execute batch insert
    int[][] results = baseRepository.insertBatch(sql, users, 1000, (ps, user) -> {
        try {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getName());
            ps.setInt(3, user.getAge());
            ps.setObject(4, user.getCreatedAt());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set parameters", e);
        }
    });
    
    // Check results
    int totalInserted = baseRepository.getTotalAffectedRows(results);
    log.info("Inserted {} users in {} batches", totalInserted, results.length);
}
```

### 4.2. Batch Update

```java
public void updateBatch(List<User> users) {
    String sql = """
        UPDATE users 
        SET name = ?, age = ? 
        WHERE id = ?
        """;
    
    int[][] results = baseRepository.updateBatch(sql, users, 1000, (ps, user) -> {
        try {
            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setLong(3, user.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set parameters", e);
        }
    });
    
    // Verify all updates succeeded
    if (!baseRepository.isAllBatchSuccess(results)) {
        throw new RuntimeException("Some updates failed");
    }
}
```

### 4.3. Batch Delete

```java
public void deleteBatch(List<Long> userIds) {
    String sql = "DELETE FROM users WHERE id = ?";
    
    int[][] results = baseRepository.deleteBatch(sql, userIds, 1000, (ps, id) -> {
        try {
            ps.setLong(1, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set parameter", e);
        }
    });
    
    int totalDeleted = baseRepository.getTotalAffectedRows(results);
    log.info("Deleted {} users", totalDeleted);
}
```

### 4.4. Hiểu kết quả Batch Operation

```java
int[][] results = baseRepository.insertBatch(sql, users, 1000, setter);

// Ví dụ kết quả: [[1,1,1,...,1], [1,1,1,...]]
// - results.length = số lượng batches (ví dụ: 2 batches)
// - results[0].length = số rows trong batch đầu tiên (ví dụ: 1000 rows)
// - results[1].length = số rows trong batch thứ hai (ví dụ: 500 rows)
// - Mỗi giá trị = 1 nghĩa là 1 row affected (thành công)

// Tính tổng rows affected
int totalAffected = baseRepository.getTotalAffectedRows(results);
System.out.println("Total inserted: " + totalAffected);

// Check tất cả có thành công không
boolean allSuccess = baseRepository.isAllBatchSuccess(results);
if (!allSuccess) {
    // Handle partial failure
    log.warn("Some batch operations failed");
}
```

## 🎯 Ví dụ thực tế

### Service Layer hoàn chỉnh

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final BaseRepository baseRepository;
    
    // Create single user
    public User createUser(UserCreateDto dto) {
        // Validate
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        
        // Create
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setAge(dto.getAge());
        user.setCreatedAt(LocalDateTime.now());
        
        Long id = userRepository.createAndGetId(user);
        user.setId(id);
        
        return user;
    }
    
    // Get user by ID
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
    
    // Update user
    public User updateUser(Long id, UserUpdateDto dto) {
        User user = getUser(id);
        
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        
        userRepository.update(user);
        return user;
    }
    
    // Delete user
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new NotFoundException("User not found");
        }
        userRepository.delete(id);
    }
    
    // Bulk import users
    public void importUsers(List<UserImportDto> dtos) {
        // Convert DTOs to entities
        List<User> users = dtos.stream()
            .map(dto -> {
                User user = new User();
                user.setEmail(dto.getEmail());
                user.setName(dto.getName());
                user.setAge(dto.getAge());
                user.setCreatedAt(LocalDateTime.now());
                return user;
            })
            .toList();
        
        // Batch insert (1000 records per batch)
        userRepository.createBatch(users);
    }
    
    // Complex query
    public List<User> findActiveUsersAboveAge(int minAge) {
        String sql = """
            SELECT u.* FROM users u
            WHERE u.age >= :minAge
            AND u.created_at >= :cutoffDate
            ORDER BY u.created_at DESC
            """;
        
        return baseRepository.select(
            sql,
            Map.of(
                "minAge", minAge,
                "cutoffDate", LocalDateTime.now().minusMonths(6)
            ),
            User.class
        );
    }
}
```

## 🔧 Best Practices

### 1. Luôn sử dụng Named Parameters

✅ **ĐÚNG:**
```java
String sql = "SELECT * FROM users WHERE email = :email";
Map<String, Object> params = Map.of("email", email);
```

❌ **SAI:**
```java
String sql = "SELECT * FROM users WHERE email = ?";
```

### 2. Validate Input

```java
public User getUser(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("Invalid user ID");
    }
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
}
```

### 3. Sử dụng Transaction

```java
@Transactional
public void transferData(Long fromId, Long toId) {
    User from = getUser(fromId);
    User to = getUser(toId);
    
    // Multiple operations in one transaction
    userRepository.update(from);
    userRepository.update(to);
}
```

### 4. Batch Size tối ưu

```java
// Good for most cases
baseRepository.insertBatch(sql, data, 1000, setter);

// For very large datasets
baseRepository.insertBatch(sql, data, 5000, setter);

// For complex objects or slow network
baseRepository.insertBatch(sql, data, 500, setter);
```

### 5. Sử dụng Optional đúng cách

```java
// ✅ ĐÚNG: Return Optional cho single result
public Optional<User> findByEmail(String email) {
    return baseRepository.findOne(sql, params, User.class);
}

// ✅ ĐÚNG: Throw exception trong service layer
public User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
}

// ❌ SAI: Return null
public User findByEmail(String email) {
    return baseRepository.find(sql, params, User.class); // Throws if not found
}
```

## ⚠️ Lưu ý quan trọng

1. **Thread Safety**: Framework đã thread-safe, có thể sử dụng trong môi trường multi-thread

2. **Performance**:
  - Batch operations nhanh hơn 10-100x so với insert từng record
  - RowMapper được cache, không cần lo về performance

3. **Error Handling**:
  - Framework throw exception rõ ràng khi có lỗi
  - Luôn wrap trong try-catch hoặc dùng @Transactional

4. **Column Naming**:
  - Database: snake_case (`created_at`)
  - Java: camelCase (`createdAt`)
  - Framework tự động map

5. **Pagination**:
  - Page number bắt đầu từ 0
  - Page size nên là 20-100 cho web, 1000-5000 cho export

## 🆚 So sánh với JPA

| Feature             | BaseRepository | JPA         |
|---------------------|----------------|-------------|
| Tốc độ              | ⚡⚡⚡⚡⚡          | ⚡⚡⚡         |
| SQL Control         | ✅ Full control | ❌ Limited   |
| Batch Insert        | ✅ Rất nhanh    | ⚠️ Chậm hơn |
| Learning Curve      | ✅ Đơn giản     | ⚠️ Phức tạp |
| Compile-time Safety | ✅ Yes          | ✅ Yes       |
| Native Query        | ✅ Native       | ⚠️ JPQL/HQL |

```yml
spring:
  transaction:
    default-timeout: 30  # 30 seconds cho tất cả transactions
    
  datasource:
    hikari:
      connection-timeout: 30000  # 30 seconds
      validation-timeout: 5000   # 5 seconds
```