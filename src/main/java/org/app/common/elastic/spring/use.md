```java
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "...") // Enable only Elasticsearch
@Import({ElasticsearchConfig.class})  // Import shared configurations
public class MySpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }
}
```