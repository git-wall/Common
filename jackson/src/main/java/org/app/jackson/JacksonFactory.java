package org.app.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public class JacksonFactory {
    // Pre-configured mapper names
    public static final String MAPPER_CASE_INSENSITIVE = "case_insensitive";
    public static final String MAPPER_SNAKE_CASE = "snake_case";
    public static final String MAPPER_FAIL_ON_UNKNOWN = "fail_on_unknown";
    public static final String MAPPER_EAV = "eav";

    static {
        // Afterburner generates bytecode for:
        // - Direct field access (no reflection)
        // - Optimized type checking
        // - Faster serialization/deserialization (10 - 30% speed improvement)
        // Java 8/11 is ok, Java > 17 no need this
        var afterburnerModule = new AfterburnerModule();

        var factory = JsonFactory.builder()
            // ✅ Enable faster string parsing
            .enable(JsonFactory.Feature.INTERN_FIELD_NAMES)
            // ✅ Recycle buffers
            .enable(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING)
            .build();

        // Initialize default mapper (only for DTO serialization/deserialization)
        JacksonUtils.DEFAULT_MAPPER = JsonMapper.builder(factory)
            .addModule(afterburnerModule)
            // SERIALIZATION
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // DESERIALIZATION
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            // Enable reading unknown enum values as null
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            // FIELD, GETTER, SETTER, CREATOR ❌
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            // FIELD ✅
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

        JacksonUtils.DEFAULT_READER = JacksonUtils.DEFAULT_MAPPER.reader();
        JacksonUtils.DEFAULT_WRITER = JacksonUtils.DEFAULT_MAPPER.writer();
        JacksonUtils.DEFAULT_WRITER_PRETTY = JacksonUtils.DEFAULT_MAPPER.writerWithDefaultPrettyPrinter();
        JacksonUtils.TF = JacksonUtils.DEFAULT_MAPPER.getTypeFactory();
    }

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Case insensitive mapper
        JacksonUtils.registerMapper(MAPPER_CASE_INSENSITIVE, JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .addModule(javaTimeModule)
            .build());
    }

    static {
        // Snake case mapper
        JacksonUtils.registerMapper(MAPPER_SNAKE_CASE, new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL));
    }

    static {
        // Strict mapper (fail on unknown properties)
        JacksonUtils.registerMapper(MAPPER_FAIL_ON_UNKNOWN, new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL));
    }

    static {
        // DTO have dynamic field (Map<String, Object> / EAV)
        // so we disable intern field names to avoid OOM
        var factory = JsonFactory.builder()
            .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
            .enable(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING)
            .build();
        var mapperEAV = JsonMapper.builder(factory)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            // FIELD, GETTER, SETTER, CREATOR ❌
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            // FIELD ✅
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

        JacksonUtils.registerMapper(MAPPER_EAV, mapperEAV);
    }
}
