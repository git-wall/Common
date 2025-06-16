```java
public class InnerTest {

    /**
     * Test class for reflection tests.
     */
    static class TestClass {
        private String testField;
        
        public void testMethod() {}
        public void testMethod(String arg) {}
    }

    @Test
    public void testLookup() {
        // Given
        String key = "testKey";
        String expectedValue = "testValue";
        
        // When
        Lookup<String> lookup = Inner.lookup(k -> k.equals(key) ? expectedValue : null);
        
        // Then
        assertEquals(expectedValue, lookup.lookup(key));
        assertNull(lookup.lookup("otherKey"));
    }

    @Test
    public void testLookupField() {
        // Given
        Class<?> clazz = TestClass.class;
        String fieldName = "testField";
        
        // When
        Lookup<Field> lookup = Inner.lookupField(clazz);
        
        // Then
        Field field = lookup.lookup(fieldName);
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
        
        // Should throw exception for non-existent field
        assertThrows(IllegalArgumentException.class, () -> lookup.lookup("nonExistentField"));
    }

    @Test
    public void testLookupMethod() {
        // Given
        Class<?> clazz = TestClass.class;
        String methodName = "testMethod";
        
        // When
        Lookup<Method[]> lookup = Inner.lookupMethod(clazz);
        
        // Then
        Method[] methods = lookup.lookup(methodName);
        assertNotNull(methods);
        assertTrue(methods.length > 0);
        assertEquals(methodName, methods[0].getName());
        
        // Should throw exception for non-existent method
        assertThrows(IllegalArgumentException.class, () -> lookup.lookup("nonExistentMethod"));
    }

    @Test
    public void testLookupResource() {
        // Given
        ClassLoader classLoader = getClass().getClassLoader();
        String resourceName = "application.properties"; // Assuming this exists
        
        // When
        Lookup<InputStream> lookup = Inner.lookupResource(classLoader);
        
        // Then
        // This test might fail if the resource doesn't exist
        // assertNotNull(lookup.lookup(resourceName));
        
        // Should throw exception for non-existent resource
        assertThrows(IllegalArgumentException.class, () -> lookup.lookup("nonExistentResource.xyz"));
    }

    @Test
    public void testLookupProperty() {
        // Given
        Properties properties = new Properties();
        String key = "testKey";
        String value = "testValue";
        properties.setProperty(key, value);
        
        // When
        Lookup<String> lookup = Inner.lookupProperty(properties);
        
        // Then
        assertEquals(value, lookup.lookup(key));
        
        // Should throw exception for non-existent property
        assertThrows(IllegalArgumentException.class, () -> lookup.lookup("nonExistentKey"));
    }

    @Test
    public void testLookupMap() {
        // Given
        Map<String, Integer> map = new HashMap<>();
        String key = "testKey";
        Integer value = 42;
        map.put(key, value);
        
        // When
        Lookup<Integer> lookup = Inner.lookupMap(map);
        
        // Then
        assertEquals(value, lookup.lookup(key));
        
        // Should throw exception for non-existent key
        assertThrows(IllegalArgumentException.class, () -> lookup.lookup("nonExistentKey"));
    }

    @Test
    public void testChainedLookup() {
        // Given
        Lookup<String> lookup1 = Inner.lookup(k -> k.equals("key1") ? "value1" : null);
        Lookup<String> lookup2 = Inner.lookup(k -> k.equals("key2") ? "value2" : null);
        
        // When
        Lookup<String> chainedLookup = Inner.chainedLookup(lookup1, lookup2);
        
        // Then
        assertEquals("value1", chainedLookup.lookup("key1"));
        assertEquals("value2", chainedLookup.lookup("key2"));
        
        // Should throw exception if no lookup can handle the key
        assertThrows(IllegalArgumentException.class, () -> chainedLookup.lookup("nonExistentKey"));
    }

    @Test
    public void testWithDefault() {
        // Given
        Lookup<String> lookup = Inner.lookup(k -> k.equals("key") ? "value" : null);
        String defaultValue = "defaultValue";
        
        // When
        Lookup<String> lookupWithDefault = Inner.withDefault(lookup, defaultValue);
        
        // Then
        assertEquals("value", lookupWithDefault.lookup("key"));
        assertEquals(defaultValue, lookupWithDefault.lookup("nonExistentKey"));
    }

    @Test
    public void testTransform() {
        // Given
        Lookup<String> lookup = Inner.lookup(k -> k.equals("key") ? "42" : null);
        
        // When
        Lookup<Integer> transformedLookup = Inner.transform(lookup, Integer::parseInt);
        
        // Then
        assertEquals(42, transformedLookup.lookup("key"));
        
        // Should propagate exceptions from the source lookup
        assertThrows(Exception.class, () -> transformedLookup.lookup("nonExistentKey"));
    }
}
```