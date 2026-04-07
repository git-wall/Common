```java
public void pipeline(){
    Pipeline.from(findAll())
            .then(e -> redis.setIfAbsentWithTimeout(e, KeyCache.DISTRICT_BY_ID_KEY, District::getDistrictId))
            .map(e -> e.stream().collect(Collectors.groupingBy(District::getProvinceId)))
            .then(e -> redis.setIfAbsentWithTimeout(e, KeyCache.DISTRICT_BY_PROVINCE_KEY))
            .sink(e -> e.getOrDefault(provinceId, Collections.emptyList()))
            .get();
}
```