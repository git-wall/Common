
# Elasticsearch Service Documentation

This document provides comprehensive examples of how to use the ElasticsearchService class for interacting with Elasticsearch.

## Table of Contents
- [Basic Index Operations](#basic-index-operations)
- [Document Operations](#document-operations)
- [Basic Search Operations](#basic-search-operations)
- [Advanced Search Operations](#advanced-search-operations)
- [Pagination and Sorting](#pagination-and-sorting)
- [Aggregations](#aggregations)
- [Complete Example](#complete-example)

## Basic Index Operations

### Creating an Index

```java
// Create a new index
boolean created = elasticsearchService.createIndex("products");
```
```java
// Check if an index exists
boolean exists = elasticsearchService.indexExists("products");
```

```java
// Delete an index
boolean deleted = elasticsearchService.deleteIndex("products");
```

```java
// Define a product class
public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
}

// Index a document
Product product = new Product("1", "Laptop", "Electronics", 999.99);
elasticsearchService.indexDocument("products", product.getId(), product);
```

```java
// Update a document
Product updatedProduct = new Product("1", "Gaming Laptop", "Electronics", 1299.99);
elasticsearchService.updateDocument("products", updatedProduct.getId(), updatedProduct);
```

```java
// Delete a document
elasticsearchService.deleteDocument("products", "1");
```

```java
// Search for all documents
Collection<Product> products = elasticsearchService.searchDocuments(
        "products",
        Product.class,
        q -> q.matchAll(m -> m)
    );
```

```java
// Search for documents matching a specific field
 Collection<Product> products = elasticsearchService.searchDocuments(
        "products",
        Product.class,
        q -> q.match(m -> m
            .field("category")
            .query("Electronics")
        )
    );
```

```java
// Perform a boolean query with multiple conditions
Collection<Product> products = elasticsearchService.searchDocuments(
        "products",
        Product.class,
        q -> q.bool(b -> b
            .must(m -> m
                .match(t -> t
                    .field("category")
                    .query("Electronics")
                )
            )
            .filter(f -> f
                .range(r -> r
                    .field("price")
                    .gte("500")
                    .lte("1500")
                )
            )
        )
    );
```

```java
// Search across multiple fields

 Collection<Product> products = elasticsearchService.multiMatchQuery(
        "products",
        Product.class,
        "gaming laptop",
        "name", "description"
    );
```

```java
// Exact term matching

Collection<Product> products = elasticsearchService.termQuery(
        "products",
        Product.class,
        "category.keyword",
        "Electronics"
    );
```
```java
// Range query for numeric values

 Collection<Product> products = elasticsearchService.rangeQuery(
        "products",
        Product.class,
        "price",
        500,
        1000
    );
```

```java
// Fuzzy search for approximate matching

Collection<Product> products = elasticsearchService.fuzzyQuery(
        "products",
        Product.class,
        "name",
        "labtop",  // Misspelled "laptop"
        "AUTO"
    );
```

```java
// Paginated search with sorting

// Import required classes
import co.elastic.clients.elasticsearch._types.SortOrder;

// Perform paginated search
ElasticsearchService.SearchResult<Product> result = elasticsearchService.searchWithPagination(
        "products",
        Product.class,
        q -> q.matchAll(m -> m),
        0,  // from (first page)
        10, // size (10 items per page)
        s -> s.field(f -> f.field("price").order(SortOrder.Desc))
);

        // Check if there are more pages
    if (result.hasNext()) {
        System.out.println("Has next page");

        // Get next page
        ElasticsearchService.SearchResult<Product> nextPage = elasticsearchService.searchWithPagination(
                "products",
                Product.class,
                q -> q.matchAll(m -> m),
                result.getFrom() + result.getSize(),  // from (next page)
                result.getSize(),                     // size (same page size)
                s -> s.field(f -> f.field("price").order(SortOrder.Desc))
        );
    }
```

```java
// Perform statistics aggregation

Map<String, Function<Aggregation.Builder, ObjectBuilder<Aggregation>>> aggregations = new HashMap<>();
    
    // Add a stats aggregation on the price field
    aggregations.put("price_stats", a -> a
        .stats(s -> s
            .field("price")
        )
    );
    
    Map<String, Object> results = elasticsearchService.searchWithAggregations(
        "products",
        Product.class,
        q -> q.matchAll(m -> m),
        aggregations
    );
    
    // Access the aggregation results
    @SuppressWarnings("unchecked")
    Map<String, Object> priceStats = (Map<String, Object>) results.get("price_stats");
```

```java
// Perform terms aggregation

Map<String, Function<Aggregation.Builder, ObjectBuilder<Aggregation>>> aggregations = new HashMap<>();
    
    // Add a terms aggregation on the category field
    aggregations.put("categories", a -> a
        .terms(t -> t
            .field("category.keyword")
            .size(10)
        )
    );
    
    Map<String, Object> results = elasticsearchService.searchWithAggregations(
        "products",
        Product.class,
        q -> q.matchAll(m -> m),
        aggregations
    );
    
    // Access the aggregation results
    // Note: The structure of aggregation results can be complex
    // You may need to cast and navigate through nested maps
    System.out.println("Categories aggregation: " + results.get("categories"));
    
    // Typically, you would navigate through the structure like this:
    @SuppressWarnings("unchecked")
    Map<String, Object> categoriesAgg = (Map<String, Object>) results.get("categories");
    
    @SuppressWarnings("unchecked")
    Map<String, Object> bucketsContainer = (Map<String, Object>) categoriesAgg.get("buckets");
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> buckets = (List<Map<String, Object>>) bucketsContainer.get("array");
    
    for (Map<String, Object> bucket : buckets) {
        String key = (String) bucket.get("key");
        Number docCount = (Number) bucket.get("doc_count");
        System.out.println("Category: " + key + ", Count: " + docCount);
    }
```

```java
// Perform date histogram aggregation
Map<String, Function<Aggregation.Builder, ObjectBuilder<Aggregation>>> aggregations = new HashMap<>();

// Add a date histogram aggregation on a date field
    aggregations.put("sales_over_time", a -> a
        .dateHistogram(dh -> dh
        .field("sale_date")
            .calendarInterval(c -> c.month(m -> m))
        .format("yyyy-MM")
        )
                );

Map<String, Object> results = elasticsearchService.searchWithAggregations(
        "products",
        Product.class,
        q -> q.matchAll(m -> m),
        aggregations
);
```


```java
@Component
public class ElasticsearchExample implements CommandLineRunner {

    private final ElasticsearchService elasticsearchService;

    @Autowired
    public ElasticsearchExample(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create index if it doesn't exist
        if (!elasticsearchService.indexExists("products")) {
            elasticsearchService.createIndex("products");
            System.out.println("Created products index");
        }

        // Index some sample products
        Product product1 = new Product("1", "Laptop", "Electronics", 999.99);
        Product product2 = new Product("2", "Smartphone", "Electronics", 699.99);
        Product product3 = new Product("3", "Headphones", "Audio", 149.99);
        Product product4 = new Product("4", "Gaming Laptop", "Electronics", 1499.99);
        Product product5 = new Product("5", "Tablet", "Electronics", 349.99);

        elasticsearchService.indexDocument("products", product1.getId(), product1);
        elasticsearchService.indexDocument("products", product2.getId(), product2);
        elasticsearchService.indexDocument("products", product3.getId(), product3);
        elasticsearchService.indexDocument("products", product4.getId(), product4);
        elasticsearchService.indexDocument("products", product5.getId(), product5);

        System.out.println("Indexed 5 products");

        // Wait for indexing to complete
        Thread.sleep(1000);

        // Basic search - match all
        Collection<Product> allProducts = elasticsearchService.searchDocuments(
            "products",
            Product.class,
            q -> q.matchAll(m -> m)
        );

        System.out.println("\nAll products (" + allProducts.size() + "):");
        allProducts.forEach(System.out::println);

        // Search by category
        Collection<Product> electronicsProducts = elasticsearchService.searchDocuments(
            "products",
            Product.class,
            q -> q.match(m -> m
                .field("category")
                .query("Electronics")
            )
        );

        System.out.println("\nElectronics products (" + electronicsProducts.size() + "):");
        electronicsProducts.forEach(System.out::println);

        // Range query - products between $300 and $1000
        Collection<Product> midRangeProducts = elasticsearchService.rangeQuery(
            "products",
            Product.class,
            "price",
            300,
            1000
        );

        System.out.println("\nProducts between $300 and $1000 (" + midRangeProducts.size() + "):");
        midRangeProducts.forEach(System.out::println);

        // Multi-match query
        Collection<Product> gamingProducts = elasticsearchService.multiMatchQuery(
            "products",
            Product.class,
            "gaming",
            "name", "description"
        );

        System.out.println("\nGaming products (" + gamingProducts.size() + "):");
        gamingProducts.forEach(System.out::println);

        // Paginated search with sorting
        ElasticsearchService.SearchResult<Product> paginatedResult = elasticsearchService.searchWithPagination(
            "products",
            Product.class,
            q -> q.matchAll(m -> m),
            0,  // from
            2,  // size (2 items per page)
            s -> s.field(f -> f.field("price").order(SortOrder.Desc))
        );

        System.out.println("\nPaginated search (page 1, sorted by price desc):");
        System.out.println("Total hits: " + paginatedResult.getTotalHits());
        System.out.println("Total pages: " + paginatedResult.getTotalPages());
        paginatedResult.getDocuments().forEach(System.out::println);

        // Price statistics aggregation
        Map<String, Function<Aggregation.Builder, ObjectBuilder<Aggregation>>> aggregations = new HashMap<>();
        aggregations.put("price_stats", a -> a
            .stats(s -> s
                .field("price")
            )
        );

        Map<String, Object> aggResults = elasticsearchService.searchWithAggregations(
            "products",
            Product.class,
            q -> q.matchAll(m -> m),
            aggregations
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> priceStats = (Map<String, Object>) aggResults.get("price_stats");

        System.out.println("\nPrice statistics:");
        System.out.println("Avg price: " + priceStats.get("avg"));
        System.out.println("Min price: " + priceStats.get("min"));
        System.out.println("Max price: " + priceStats.get("max"));
        System.out.println("Sum price: " + priceStats.get("sum"));
        System.out.println("Count: " + priceStats.get("count"));

        // Clean up - delete the index
        // Uncomment if you want to delete the index after the example
        // elasticsearchService.deleteIndex("products");
        // System.out.println("\nDeleted products index");
    }

    // Product class for this example
    public static class Product {
        private String id;
        private String name;
        private String category;
        private double price;

        public Product() {
        }

        public Product(String id, String name, String category, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        @Override
        public String toString() {
            return "Product{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", category='" + category + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
}
```