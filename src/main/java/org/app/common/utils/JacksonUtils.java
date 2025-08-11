package org.app.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
public class JacksonUtils {

    protected static final ObjectMapper MAPPER;
    // Instead, expose immutable reader and writer for advanced use cases.
    protected static final ObjectReader READER;
    protected static final ObjectWriter WRITER;
    protected static final ObjectWriter WRITER_PRETTY;
    protected static final TypeFactory TF;

    private JacksonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    static {
        MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        READER = MAPPER.reader();
        WRITER = MAPPER.writer();
        WRITER_PRETTY = MAPPER.writerWithDefaultPrettyPrinter();
        TF = MAPPER.getTypeFactory();
    }

    /**
     * Returns an ObjectReader for advanced use cases.
     */
    public static ObjectReader reader() {
        return READER;
    }

    /**
     * Returns an ObjectWriter for advanced use cases.
     */
    public static ObjectWriter writer() {
        return WRITER;
    }

    @SneakyThrows
    public static <T> T readValue(Object json, JavaType type) {
        return MAPPER.readValue(json.toString(), type);
    }

    @SneakyThrows
    public static <T> T readValue(Object json, Class<T> clazz) {
        return MAPPER.readValue(json.toString(), clazz);
    }

    @SneakyThrows
    public static <K, V> Map<K, V> readToMap(Object json, Class<K> key, Class<V> value) {
        return MAPPER.readValue(json.toString(), typeOf(Map.class, key, value));
    }

    @SneakyThrows
    public static <T> List<T> readToList(Object json, Class<T> elementType) {
        return MAPPER.readValue(json.toString(), typeOf(List.class, elementType));
    }

    @SneakyThrows
    public static <T> T convert(Object json, Class<T> clazz) {
        return MAPPER.convertValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T convert(Object json, JavaType type) {
        return MAPPER.convertValue(json.toString(), type);
    }

    public static <K, V> Map<K, V> convertMap(Object json, Class<K> key, Class<V> value) {
        return MAPPER.convertValue(json, typeOf(Map.class, key, value));
    }

    public static <T> List<T> convertList(Object json, Class<T> elementType) {
        return MAPPER.convertValue(json, typeOf(List.class, elementType));
    }

    /**
     * Dynamic conversion based on runtime class name
     */
    @SuppressWarnings("unchecked")
    public <T> T convertByClassName(Object source, String targetClassName) {
        try {
            Class<T> targetClass = (Class<T>) Class.forName(targetClassName);
            return convert(source, targetClass);

        } catch (ClassNotFoundException e) {
            throw new ConversionException("Target class not found: " + targetClassName, e);
        }
    }

    //-----------------------------JAVA TYPE---------------------------------------//
    // with java type it still has cache, so we not need to create cache it makes flow is double caching

    /**
     * Builds a JavaType for a simple type or nested generic type.
     * <pre> {@code
     *   typeOf(List.class, String.class) -> List<String>
     *   typeOf(Map.class, String.class, Integer.class) -> Map<String, Integer>
     *   typeOf(Map.class, String.class, List.class, MyDto.class) -> Map<String, List<MyDto>>
     * }</>
     */
    public static JavaType typeOf(Class<?> mainType, Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            // No generics, just a simple type
            return typeOf(mainType);
        }

        // Recursively resolve nested generics
        JavaType[] javaParamTypes = new JavaType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParamTypes[i] = TF.constructType(parameterTypes[i]);
        }

        return TF.constructParametricType(mainType, javaParamTypes);
    }

    //----------------------------------------------------------------------------//

    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * Get an object as JsonNode for manipulation
     */
    public JsonNode getAsJsonNode(Object source) {
        if (source == null) {
            return MAPPER.nullNode();
        }
        return MAPPER.valueToTree(source);
    }

    /**
     * Convert JsonNode to target class
     */
    @SneakyThrows
    public <T> T convertJsonNode(JsonNode jsonNode, Class<T> targetClass) {
        try {
            return MAPPER.treeToValue(jsonNode, targetClass);
        } catch (Exception e) {
            throw new ConversionException("Failed to convert JsonNode to " + targetClass.getSimpleName(), e);
        }
    }

    @SneakyThrows
    public static String writeValueAsString(Object o) {
        return MAPPER.writeValueAsString(o);
    }

    /**
     * increase from default of 20 MB to 20 MiB (note megabytes vs mebibyte)
     */
    public static ObjectMapper newMapperMax20MIB() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
            StreamReadConstraints.defaults().rebuild()
                .maxStringLength(20 * 1024 * 1024)
                .build()
        );
        return mapper;
    }

    @NonNull
    public static ObjectMapper newMapperWithCaseInsensitive(String dateFormat) {
        ObjectMapper mapper = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.setDateFormat(new SimpleDateFormat(dateFormat));
        return mapper;
    }

    public static ObjectMapper newMapperWithCaseInsensitive() {
        return JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .build();
    }

    @SneakyThrows
    public static String toJson(Object data) {
        try {
            return WRITER_PRETTY.writeValueAsString(data);
        } catch (InvalidDefinitionException e) {
            return data.toString();
        } catch (JsonProcessingException e) {
            log.error("Error occurred while parsing json", e);
            // handler more case has special char that make JSON invalid and not parseable and error
            return WRITER_PRETTY.writeValueAsString(e).replace('%', ' ');
        }
    }

    @SneakyThrows
    public static JsonNode readTree(String data) {
        return MAPPER.readTree(data);
    }

    public static void replaceNullStrings(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
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

    /**
     * Deep clone object
     */
    public <T> T deepClone(T source) {
        if (source == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) source.getClass();
        return convert(source, clazz);
    }

    /**
     * Custom exception for conversion errors
     */
    public static class ConversionException extends RuntimeException {
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
