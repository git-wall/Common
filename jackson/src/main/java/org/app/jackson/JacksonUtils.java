package org.app.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.app.exception.SlightException;
import org.app.exception.StackLocators;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtils {

    // Default mapper
    protected static ObjectMapper DEFAULT_MAPPER;
    protected static ObjectReader DEFAULT_READER;
    protected static ObjectWriter DEFAULT_WRITER;
    protected static ObjectWriter DEFAULT_WRITER_PRETTY;
    protected static TypeFactory TF;

    // Mapper registry for reusable custom mappers
    private static final Map<String, ObjectMapper> MAPPER_REGISTRY = new ConcurrentHashMap<>();

    // ============ MAPPER REGISTRY MANAGEMENT ============

    /**
     * Register a custom mapper with a unique name
     */
    public static void registerMapper(String name, ObjectMapper mapper) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mapper name cannot be null or empty");
        }
        MAPPER_REGISTRY.put(name, mapper);
    }

    /**
     * Register a mapper with custom configuration
     */
    public static void registerMapper(String name, Consumer<ObjectMapper> configurator) {
        ObjectMapper mapper = new ObjectMapper();
        configurator.accept(mapper);
        registerMapper(name, mapper);
    }

    /**
     * Get a registered mapper by name
     */
    public static ObjectMapper getMapper(String name) {
        ObjectMapper mapper = MAPPER_REGISTRY.get(name);
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper not found: " + name);
        }
        return mapper;
    }

    /**
     * Get default mapper
     */
    public static ObjectMapper mapper() {
        return DEFAULT_MAPPER;
    }

    /**
     * Check if a mapper exists
     */
    public static boolean hasMapper(String name) {
        return MAPPER_REGISTRY.containsKey(name);
    }

    /**
     * Get all registered mapper names
     */
    public static Set<String> getRegisteredMapperNames() {
        return new HashSet<>(MAPPER_REGISTRY.keySet());
    }

    /**
     * Remove a mapper from registry
     */
    public static void unregisterMapper(String name) {
        MAPPER_REGISTRY.remove(name);
    }

    // ============ READ OPERATIONS WITH MAPPER SELECTION ============

    public static <T> T readValue(Object json, Class<T> clazz) {
        return readValue(json, clazz, null);
    }

    public static <T> T readValue(Object json, Class<T> clazz, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), clazz);
        } catch (JsonProcessingException e) {
            throw ReadException.of(clazz, e);
        }
    }

    public static <T> T readValue(byte[] json, JavaType type) {
        try {
            return DEFAULT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw ReadException.of(type, e);
        }
    }

    public static <T> T readValue(InputStream json, JavaType type) {
        try {
            return DEFAULT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw ReadException.of(type, e);
        }
    }

    public static <T> T readValue(Object json, JavaType type) {
        return readValue(json, type, null);
    }

    public static <T> T readValue(Object json, JavaType type, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), type);
        } catch (JsonProcessingException e) {
            throw ReadException.of(type, e);
        }
    }

    public static <K, V> Map<K, V> readToMap(Object json, Class<K> key, Class<V> value) {
        return readToMap(json, key, value, null);
    }

    public static <K, V> Map<K, V> readToMap(Object json, Class<K> key, Class<V> value, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), typeMapOf(key, value));
        } catch (JsonProcessingException e) {
            throw ReadException.ofMap(key, value, e);
        }
    }

    public static <T> List<T> readToList(Object json, Class<T> elementType) {
        return readToList(json, elementType, null);
    }

    public static <T> List<T> readToList(Object json, Class<T> elementType, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), typeListOf(elementType));
        } catch (JsonProcessingException e) {
            throw ReadException.ofList(elementType, e);
        }
    }

    // ============ CONVERT OPERATIONS WITH MAPPER SELECTION ============
    public static <T> T convert(Object json, JavaType type) {
        return DEFAULT_MAPPER.convertValue(json, type);
    }

    public static <T> T convert(Object json, JavaType type, String mapperName) {
        return getMapper(mapperName).convertValue(json, type);
    }

    public static <T> T convert(Object json, Class<T> clazz) {
        return convert(json, clazz, null);
    }

    public static <T> T convert(Object json, Class<T> clazz, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.convertValue(json, clazz);
        } catch (IllegalArgumentException e) {
            throw ConversionException.of(clazz, e);
        }
    }

    public static <K, V> Map<K, V> convertMap(Object json, Class<K> key, Class<V> value) {
        return convertMap(json, key, value, null);
    }

    public static <K, V> Map<K, V> convertMap(Object json, Class<K> key, Class<V> value, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.convertValue(json, typeOf(Map.class, key, value));
        } catch (IllegalArgumentException e) {
            throw ConversionException.ofMap(key, value, e);
        }
    }

    public static <T> List<T> convertList(Object json, Class<T> elementType) {
        return convertList(json, elementType, null);
    }

    public static <T> List<T> convertList(Object json, Class<T> elementType, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.convertValue(json, typeOf(List.class, elementType));
        } catch (IllegalArgumentException e) {
            throw ConversionException.ofList(elementType, e);
        }
    }

    // ============ JAVA TYPE FACTORY ============

    public static JavaType mapType(Class<?> key, Class<?> value) {
        return TF.constructMapType(Map.class, key, value);
    }

    public static JavaType mapType(JavaType keyType, JavaType valueType) {
        return TF.constructMapType(Map.class, keyType, valueType);
    }

    public static JavaType listType(Class<?> elementType) {
        return TF.constructCollectionType(List.class, elementType);
    }

    public static JavaType setType(Class<?> elementType) {
        return TF.constructCollectionType(Set.class, elementType);
    }

    public static JavaType typeOf(Class<?> mainType) {
        return TF.constructType(mainType);
    }

    public static JavaType typeListOf(Class<?> mainType) {
        return TF.constructCollectionType(List.class, mainType);
    }

    public static JavaType typeMapOf(Class<?> key, Class<?> value) {
        return TF.constructMapType(HashMap.class, key, value);
    }

    public static JavaType typeOf(Class<?> mainType, Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return TF.constructType(mainType);
        }

        JavaType[] javaParamTypes = new JavaType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParamTypes[i] = TF.constructType(parameterTypes[i]);
        }

        return TF.constructParametricType(mainType, javaParamTypes);
    }

    public static JavaType typeOf(Class<?> mainType, JavaType... javaParamTypes) {
        return TF.constructParametricType(mainType, javaParamTypes);
    }

    // ============ WRITE OPERATIONS ============

    @SneakyThrows
    public static String writeValueAsString(Object o) {
        return writeValueAsString(o, null);
    }

    @SneakyThrows
    public static String writeValueAsString(Object o, String mapperName) {
        ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
        return mapper.writeValueAsString(o);
    }

    @SneakyThrows
    public static byte[] writeValueAsBytes(Object o) {
        return writeValueAsBytes(o, null);
    }

    @SneakyThrows
    public static byte[] writeValueAsBytes(Object o, String mapperName) {
        ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
        return mapper.writeValueAsBytes(o);
    }

    @SneakyThrows
    public static String toJson(Object data) {
        return toJson(data, null);
    }

    @SneakyThrows
    public static String toJson(Object data, String mapperName) {
        try {
            ObjectWriter writer = mapperName == null
                ? DEFAULT_WRITER_PRETTY
                : getMapper(mapperName).writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(data);
        } catch (InvalidDefinitionException e) {
            return data.toString();
        } catch (JsonProcessingException e) {
            return DEFAULT_WRITER_PRETTY.writeValueAsString(e).replace('%', ' ');
        }
    }

    // ============ UTILITY METHODS (unchanged) ============

    public static ObjectReader reader() {
        return DEFAULT_READER;
    }

    public static ObjectWriter writer() {
        return DEFAULT_WRITER;
    }

    public static ObjectNode createObjectNode() {
        return DEFAULT_MAPPER.createObjectNode();
    }

    @SneakyThrows
    public static JsonNode readTree(String data) {
        return DEFAULT_MAPPER.readTree(data);
    }

    public static ObjectMapper addMixin(Class<?> target, Class<?> mixin) {
        ObjectMapper mapper = DEFAULT_MAPPER.copy();
        mapper.addMixIn(target, mixin);
        return mapper;
    }

    public <T> T deepClone(T source) {
        if (source == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) source.getClass();
        return convert(source, clazz);
    }

    // ============ EXCEPTIONS ============

    public static class ConversionException extends SlightException {
        private static final long serialVersionUID = -1693109783788356747L;

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public static ConversionException of(Class<?> clazz, Throwable cause) {
            String message = String.format("Cannot convert to %s: %s", clazz.getSimpleName(), cause.getMessage());
            return new ConversionException(message);
        }

        public static ConversionException of(JavaType type, Throwable cause) {
            String message = String.format("Cannot convert to %s: %s", type.getRawClass().getSimpleName(), cause.getMessage());
            return new ConversionException(message);
        }

        public static ConversionException ofMap(Class<?> clazzKey, Class<?> clazzVal, Throwable cause) {
            String message = String.format(
                "Cannot convert to Map<%s,%s>: %s",
                clazzKey.getSimpleName(), clazzVal.getSimpleName(), cause.getMessage());

            return new ConversionException(message);
        }

        public static ConversionException ofList(Class<?> elementType, Throwable cause) {
            String message = String.format("Cannot convert to List<%s>: %s", elementType.getSimpleName(), cause.getMessage());
            return new ConversionException(message);
        }
    }

    public static class ReadException extends SlightException {
        private static final long serialVersionUID = 2594071083304122225L;

        protected ReadException(String message) {
            super(message);
        }

        public static ReadException of(Class<?> clazz, Throwable cause) {
            String message = String.format("Failed to read to %s: %s", clazz.getSimpleName(), cause.getMessage());
            return new ReadException(message);
        }

        public static ReadException of(JavaType type, Throwable cause) {
            String message = String.format(
                "Failed to read to %s: %s", type.getRawClass().getSimpleName(), cause.getMessage());

            return new ReadException(message);
        }

        public static ReadException ofMap(Class<?> clazzKey, Class<?> clazzVal, Throwable cause) {
            String message = String.format(
                "Failed to read to Map<%s,%s>: %s",
                clazzKey.getSimpleName(), clazzVal.getSimpleName(), cause.getMessage());

            return new ReadException(message);
        }

        public static ReadException ofList(Class<?> elementType, Throwable cause) {
            String message = String.format(
                "Failed to read to List<%s>: %s", elementType.getSimpleName(), cause.getMessage());

            return new ReadException(message);
        }
    }

    static {
        StackLocators.registerIgnoreClass(ConversionException.class);
        StackLocators.registerIgnoreClass(ReadException.class);
        StackLocators.registerIgnoreClass(JacksonUtils.class);
    }
}
