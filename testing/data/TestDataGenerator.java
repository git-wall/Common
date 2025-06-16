package com.example.testing.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Test data generator for creating realistic test data
 */
public class TestDataGenerator {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // Generate test users
        List<User> users = generateUsers(50);
        
        // Generate test products
        List<Product> products = generateProducts(100);
        
        // Generate test orders
        List<Order> orders = generateOrders(200, users, products);
        
        // Create test fixtures
        TestFixtures fixtures = new TestFixtures(users, products, orders);
        
        // Write to JSON file
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File("src/test/resources/test-fixtures.json"), fixtures);
        
        System.out.println("Test data generated successfully!");
    }
    
    private static List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername(faker.name().username());
            user.setEmail(faker.internet().emailAddress());
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setCreatedAt(LocalDateTime.ofInstant(
                    faker.date().past(365, TimeUnit.DAYS).toInstant(), 
                    ZoneId.systemDefault()));
            user.setActive(random.nextBoolean());
            
            users.add(user);
        }
        
        return users;
    }
    
    private static List<Product> generateProducts(int count) {
        List<Product> products = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setName(faker.commerce().productName());
            product.setDescription(faker.lorem().paragraph());
            product.setPrice(new BigDecimal(faker.commerce().price())
                    .setScale(2, RoundingMode.HALF_UP));
            product.setSku("SKU-" + faker.number().digits(6));
            product.setInventoryCount(random.nextInt(1000));
            product.setCreatedAt(LocalDateTime.ofInstant(
                    faker.date().past(180, TimeUnit.DAYS).toInstant(), 
                    ZoneId.systemDefault()));
            
            products.add(product);
        }
        
        return products;
    }
    
    private static List<Order> generateOrders(int count, List<User> users, List<Product> products) {
        List<Order> orders = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUser(users.get(random.nextInt(users.size())));
            
            String[] statuses = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"};
            order.setStatus(statuses[random.nextInt(statuses.length)]);
            
            // Generate 1-5 order items
            int itemCount = random.nextInt(5) + 1;
            List<OrderItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (int j = 0; j < itemCount; j++) {
                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                
                Product product = products.get(random.nextInt(products.size()));
                item.setProduct(product);
                
                int quantity = random.nextInt(5) + 1;
                item.setQuantity(quantity);
                item.setUnitPrice(product.getPrice());
                
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(itemTotal);
                
                items.add(item);
            }
            
            order.setItems(items);
            order.setTotalAmount(totalAmount);
            order.setShippingAddress(faker.address().fullAddress());
            order.setBillingAddress(faker.address().fullAddress());
            order.setCreatedAt(LocalDateTime.ofInstant(
                    faker.date().past(90, TimeUnit.DAYS).toInstant(), 
                    ZoneId.systemDefault()));
            
            orders.add(order);
        }
        
        return orders;
    }
    
    @Data
    static class TestFixtures {
        private final List<User> users;
        private final List<Product> products;
        private final List<Order> orders;
    }
    
    @Data
    static class User {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private LocalDateTime createdAt;
        private boolean active;
    }
    
    @Data
    static class Product {
        private String id;
        private String name;
        private String description;
        private BigDecimal price;
        private String sku;
        private int inventoryCount;
        private LocalDateTime createdAt;
    }
    
    @Data
    static class Order {
        private String id;
        private User user;
        private String status;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private String billingAddress;
        private LocalDateTime createdAt;
        private List<OrderItem> items;
    }
    
    @Data
    static class OrderItem {
        private String id;
        private Product product;
        private int quantity;
        private BigDecimal unitPrice;
    }
}