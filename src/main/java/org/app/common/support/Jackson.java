package org.app.common.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Jackson {

    private final ObjectMapper mapper;

    public ObjectMapper map() {
        return mapper;
    }

    @SneakyThrows
    public String toJson(Object object) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @SneakyThrows
    public String toJsonCompact(Object object) {
        return mapper.writeValueAsString(object);
    }

    // Generic read methods
    @SneakyThrows
    public <T> T fromJson(String json, Class<T> clazz) {
        return mapper.readValue(json, clazz);
    }

    @SneakyThrows
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        return mapper.readValue(json, typeReference);
    }

    @SneakyThrows
    public <T> T fromJson(String json, JavaType javaType) {
        return mapper.readValue(json, javaType);
    }

    @SneakyThrows
    public <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        return mapper.readValue(inputStream, clazz);
    }

    @SneakyThrows
    public <T> T fromJson(InputStream inputStream, TypeReference<T> typeReference) {
        return mapper.readValue(inputStream, typeReference);
    }

    @SneakyThrows
    public <T> T fromJson(File file, Class<T> clazz) {
        return mapper.readValue(file, clazz);
    }

    @SneakyThrows
    public <T> T fromJson(File file, TypeReference<T> typeReference) {
        return mapper.readValue(file, typeReference);
    }

    @SneakyThrows
    public <T> T fromJson(URL url, Class<T> clazz) {
        return mapper.readValue(url, clazz);
    }

    @SneakyThrows
    public <T> T fromJson(byte[] bytes, Class<T> clazz) {
        return mapper.readValue(bytes, clazz);
    }

    // List conversion methods
    @SneakyThrows
    public <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        return mapper.readValue(json, listType);
    }

    @SneakyThrows
    public <T> Set<T> fromJsonToSet(String json, Class<T> elementClass) {
        JavaType setType = mapper.getTypeFactory().constructCollectionType(Set.class, elementClass);
        return mapper.readValue(json, setType);
    }

    @SneakyThrows
    public <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        return mapper.readValue(json, mapType);
    }

    // Array conversion methods
    @SneakyThrows
    public <T> T[] fromJsonToArray(String json, Class<T> elementClass) {
        JavaType arrayType = mapper.getTypeFactory().constructArrayType(elementClass);
        return mapper.readValue(json, arrayType);
    }

    // Object conversion methods
    @SneakyThrows
    public <T> T convertValue(Object object, Class<T> toClass) {
        return mapper.convertValue(object, toClass);
    }

    @SneakyThrows
    public <T> T convertValue(Object object, TypeReference<T> typeReference) {
        return mapper.convertValue(object, typeReference);
    }

    @SneakyThrows
    public <T> T convertValue(Object object, JavaType javaType) {
        return mapper.convertValue(object, javaType);
    }

    // Update existing object with JSON
    @SneakyThrows
    public <T> T updateValue(T objectToUpdate, String json) {
        return mapper.readerForUpdating(objectToUpdate).readValue(json);
    }

    @SneakyThrows
    public <T> T updateValue(T objectToUpdate, InputStream inputStream) {
        return mapper.readerForUpdating(objectToUpdate).readValue(inputStream);
    }

    // Tree model methods
    @SneakyThrows
    public JsonNode readTree(String json) {
        return mapper.readTree(json);
    }

    @SneakyThrows
    public JsonNode readTree(InputStream inputStream) {
        return mapper.readTree(inputStream);
    }

    @SneakyThrows
    public JsonNode readTree(File file) {
        return mapper.readTree(file);
    }

    @SneakyThrows
    public JsonNode valueToTree(Object object) {
        return mapper.valueToTree(object);
    }

    // Validation methods
    public boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Type factory helper
    public JavaType typeOf(Class<?> mainType, Class<?>... parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            // No generics, just a simple type
            return typeOf(mainType);
        }

        // Recursively resolve nested generics
        JavaType[] javaParamTypes = new JavaType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            javaParamTypes[i] = mapper.getTypeFactory().constructType(parameterTypes[i]);
        }

        return mapper.getTypeFactory().constructParametricType(mainType, javaParamTypes);
    }

    // Byte array methods
    @SneakyThrows
    public byte[] toJsonBytes(Object object) {
        return mapper.writeValueAsBytes(object);
    }

    // Clone object using JSON serialization/deserialization
    @SneakyThrows
    public <T> T clone(T object, Class<T> clazz) {
        String json = toJsonCompact(object);
        return fromJson(json, clazz);
    }

    @SneakyThrows
    public <T> T clone(T object, TypeReference<T> typeReference) {
        String json = toJsonCompact(object);
        return fromJson(json, typeReference);
    }
}

@Configuration
class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder ->
            builder.featuresToEnable(
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
                    JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
                )
                .failOnUnknownProperties(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .simpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
