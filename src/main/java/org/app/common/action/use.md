### BRDAction

```java
import org.springframework.stereotype.Service;

@Configuration
public class BRDActionConfig {

    @Bean
    public BRDAction<User> userBRDAction(RedisTemplate<String, String> redisTemplate,
                                         UserRepository userRepository) {

        QuerySupplier<User> query = QuerySupplier.<User>of()
                .exits(() -> userRepository.exits())
                .insert(() -> userRepository.insert())
                .findFields(userRepository::findAllUsernames);

        return new BRDAction<>(redisTemplate, 10_000)
                .query(query)
                .load(); // preload BloomFilter + Redis
    }
}

@Service
public class UserService {
    private final BRDAction<User> brdAction;

    public boolean checkUserExists(String username) {
        return brdAction.exists(username);
    }

    public boolean insert(User user) {
        return brdAction.register(user.getUsername(), "Username already exists");
    }
}
```

### TrieAction

```java
public class TrieConfig {

    @Bean
    public Trie<Product> productTrie(ProductRepository productRepository) {

        QuerySupplier<Product> query = QuerySupplier.<Product>of()
                .exits(() -> productRepository.exits())
                .insert(() -> productRepository.insert())
                .findFields(productRepository::findAllProductNames);

        return new TrieAction<>()
                .query(query)
                .load(); // preload Trie
    }
}
```

### CuckooFilter
1. Go to https://www.abuseipdb.com → Sign up → Get an API Key.
2. Use this endpoint to check an IP : GET https://api.abuseipdb.com/api/v2/check
- ipAddress=1.2.3.4 → The IP you want to check.
- maxAgeInDays=90 → How far back to search for reports.
```java
@Configuration
public class CuckooFilterConfig {

    @Bean
    public CuckooIPBlacklistManager cuckooManager(RedisTemplate<String, String> redisTemplate,
                                                  Environment env) {
        QuerySupplier<User> query = QuerySupplier.<User>of()
                .findFields(userRepository::findBlacklistedUsernames)
                .insert(() -> null);
        
        return new CuckooIPBlacklistManager(redisTemplate, query, env, 10_000);
    }
}
```
