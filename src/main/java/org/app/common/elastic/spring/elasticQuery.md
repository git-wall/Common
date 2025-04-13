```java
private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchServiceExample(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public ProductList findProductAdvance(ProductCriteriaDto criteria) {
        // Create a query with the new concise API
        NativeQuery searchQuery = DynamicQueryBuilder.query()
            // Add keyword search with fuzzy matching
            .fuzzyMultiMatch(criteria.keyword(), "1", 
                ProductField.NAME, ProductField.BRAND, ProductField.CATEGORIES)
            
            // Add filters for brand, category, attributes
            .termsFilter(ProductField.BRAND, criteria.brand())
            .termsFilter(ProductField.CATEGORIES, criteria.category())
            .termsFilter(ProductField.ATTRIBUTES, criteria.attribute())
            
            // Add price range filter
            .range(ProductField.PRICE, criteria.minPrice(), criteria.maxPrice())
            
            // Add filter for published products
            .term(ProductField.IS_PUBLISHED, true)
            
            // Add sorting based on sort type
            .customize(builder -> {
                if (criteria.sortType() == SortType.PRICE_ASC) {
                    builder.withSort(s -> s.field(f -> f.field(ProductField.PRICE).order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)));
                } else if (criteria.sortType() == SortType.PRICE_DESC) {
                    builder.withSort(s -> s.field(f -> f.field(ProductField.PRICE).order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));
                } else {
                    builder.withSort(s -> s.field(f -> f.field(ProductField.CREATE_ON).order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));
                }
            })
            
            // Add pagination
            .page(PageRequest.of(criteria.page(), criteria.size()))
            
            // Add aggregations
            .aggregation("categories", a -> a.terms(t -> t.field(ProductField.CATEGORIES)))
            .aggregation("attributes", a -> a.terms(t -> t.field(ProductField.ATTRIBUTES)))
            .aggregation("brands", a -> a.terms(t -> t.field(ProductField.BRAND)))
            
            .build();
        
        // Execute the query
        SearchHits<Product> searchHits = elasticsearchOperations.search(searchQuery, Product.class);
        
        // Process results (same as before)
        // ...
        
        return new ProductList(/* ... */);
    }
    
    public ProductNameList autoCompleteProductName(final String keyword) {
        // Create a query for autocomplete
        NativeQuery searchQuery = DynamicQueryBuilder.query()
            .customize(builder -> 
                builder.withQuery(q -> q.matchPhrasePrefix(m -> m.field("name").query(keyword)))
            )
            .sourceFilter(new String[]{"name"}, null)
            .build();
        
        SearchHits<Product> result = elasticsearchOperations.search(searchQuery, Product.class);
        
        List<String> productNames = result.getSearchHits().stream()
            .map(hit -> hit.getContent().getName())
            .distinct()
            .collect(Collectors.toList());
        
        return new ProductNameList(productNames);
    }
```