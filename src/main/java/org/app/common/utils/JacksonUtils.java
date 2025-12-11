package org.app.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtils {

    // Default mapper
    protected static final ObjectMapper DEFAULT_MAPPER;
    protected static final ObjectReader DEFAULT_READER;
    protected static final ObjectWriter DEFAULT_WRITER;
    protected static final ObjectWriter DEFAULT_WRITER_PRETTY;
    protected static final TypeFactory TF;

    // Mapper registry for reusable custom mappers
    private static final Map<String, ObjectMapper> MAPPER_REGISTRY = new ConcurrentHashMap<>();

    // Pre-configured mapper names
    public static final String MAPPER_CASE_INSENSITIVE = "case_insensitive";
    public static final String MAPPER_SNAKE_CASE = "snake_case";
    public static final String MAPPER_FAIL_ON_UNKNOWN = "fail_on_unknown";

    static {
        // Initialize default mapper
        DEFAULT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        DEFAULT_READER = DEFAULT_MAPPER.reader();
        DEFAULT_WRITER = DEFAULT_MAPPER.writer();
        DEFAULT_WRITER_PRETTY = DEFAULT_MAPPER.writerWithDefaultPrettyPrinter();
        TF = DEFAULT_MAPPER.getTypeFactory();

        // Register pre-configured mappers
        registerPreconfiguredMappers();
    }

    private static void registerPreconfiguredMappers() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Case insensitive mapper
        registerMapper(MAPPER_CASE_INSENSITIVE, JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .addModule(javaTimeModule)
            .build());

        // Snake case mapper
        registerMapper(MAPPER_SNAKE_CASE, new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL));

        // Strict mapper (fail on unknown properties)
        registerMapper(MAPPER_FAIL_ON_UNKNOWN, new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL));
    }

    // ============ MAPPER REGISTRY MANAGEMENT ============

    /**
     * Register a custom mapper with a unique name
     */
    public static void registerMapper(String name, ObjectMapper mapper) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mapper name cannot be null or empty");
        }
        MAPPER_REGISTRY.put(name, mapper);
        log.debug("Registered mapper: {}", name);
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
        log.debug("Unregistered mapper: {}", name);
    }

    // ============ READ OPERATIONS WITH MAPPER SELECTION ============

    @SneakyThrows
    public static <T> T readValue(Object json, Class<T> clazz) {
        return readValue(json, clazz, null);
    }

    @SneakyThrows
    public static <T> T readValue(Object json, Class<T> clazz, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), clazz);
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Can not read to Class " + clazz.getSimpleName(), e);
        }
    }

    @SneakyThrows
    public static <T> T readValue(Object json, JavaType type) {
        return readValue(json, type, null);
    }

    @SneakyThrows
    public static <T> T readValue(Object json, JavaType type, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), type);
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Can not read to Type " + type.getRawClass().getSimpleName(), e);
        }
    }

    @SneakyThrows
    public static <K, V> Map<K, V> readToMap(Object json, Class<K> key, Class<V> value) {
        return readToMap(json, key, value, null);
    }

    @SneakyThrows
    public static <K, V> Map<K, V> readToMap(Object json, Class<K> key, Class<V> value, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), typeOf(Map.class, key, value));
        } catch (IllegalArgumentException e) {
            throw new ConversionException(
                String.format("Can not read to Map<%s, %s>", key.getSimpleName(), value.getSimpleName()), e);
        }
    }

    @SneakyThrows
    public static <T> List<T> readToList(Object json, Class<T> elementType) {
        return readToList(json, elementType, null);
    }

    @SneakyThrows
    public static <T> List<T> readToList(Object json, Class<T> elementType, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.readValue(json.toString(), typeOf(List.class, elementType));
        } catch (IllegalArgumentException e) {
            throw new ConversionException(String.format("Can not read to List<%s>", elementType.getSimpleName()), e);
        }
    }

    // ============ CONVERT OPERATIONS WITH MAPPER SELECTION ============

    @SneakyThrows
    public static <T> T convert(Object json, Class<T> clazz) {
        return convert(json, clazz, null);
    }

    @SneakyThrows
    public static <T> T convert(Object json, Class<T> clazz, String mapperName) {
        try {
            ObjectMapper mapper = mapperName == null ? DEFAULT_MAPPER : getMapper(mapperName);
            return mapper.convertValue(json, clazz);
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Can not convert to Class " + clazz.getSimpleName(), e);
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
            throw new ConversionException(
                String.format("Can not convert to Map<%s, %s>", key.getSimpleName(), value.getSimpleName()), e);
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
            throw new ConversionException(String.format("Can not convert to List<%s>", elementType.getSimpleName()), e);
        }
    }

    // ============ JAVA TYPE FACTORY ============

    public static JavaType mapType(Class<?> key, Class<?> value) {
        return TF.constructMapType(Map.class, key, value);
    }

    public static JavaType listType(Class<?> elementType) {
        return TF.constructCollectionType(List.class, elementType);
    }

    public static JavaType setType(Class<?> elementType) {
        return TF.constructCollectionType(Set.class, elementType);
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
            log.error("Error occurred while parsing json", e);
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

    public static void replaceNullStrings(JsonNode node) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                JsonNode childNode = entry.getValue();
                if (childNode.isTextual() && childNode.textValue().equalsIgnoreCase("null")) {
                    ((ObjectNode) node).putNull(entry.getKey());
                } else {
                    replaceNullStrings(childNode);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                replaceNullStrings(arrayElement);
            }
        }
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

    public static class ConversionException extends RuntimeException {
        private static final long serialVersionUID = 2594071083304122225L;

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReadException extends RuntimeException {
        private static final long serialVersionUID = 2594071083304122225L;

        public ReadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
