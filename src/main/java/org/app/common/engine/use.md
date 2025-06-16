```java
String json = "{\n" +
                 "            \"PRODUCTID\": \"21321\",\n" +
                 "            \"OUTPUTTYPEID\": 3,\n" +
                 "            \"SUBGROUPID\": 3024,\n" +
                 "            \"PRICE\": 200_000.,\n" +
                 "            \"QUANTITY\": 1,\n" +
                 "            \"INVENTORYSTATUSID\": 1,\n" +
                 "            \"MAINGROUPID\": 16,\n" +
                 "            \"BRANDID\": 11243,\n" +
                 "            \"CREATEDDATE\": 1743474480000\n" +
                 "        }";

         ObjectMapper mapper = new ObjectMapper();
         // enable case-insensitive mapping (optional)
//         mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

         ProductRequest request = mapper.readValue(json, ProductRequest.class);

         List  rules = List.of(
                 new Rule(1L, "tinh diem 1", RuleType.POINT, BigDecimal.valueOf(1000),
                         "SUM(MUL(PRICE, QUANTITY), RULEVALUE)", null),
                 new Rule(2L, "tinh diem 2", RuleType.PERCENT, BigDecimal.valueOf(10),
                         "MUL(RULE_1, RULEVALUE)", Set.of(1L)
                 )
         );
    RuleEngine.evaluateRules(rules, request);
```