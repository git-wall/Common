package org.app.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.common.support.Type;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JacksonUtils {

    private static final ObjectMapper MAPPER;
    // Instead, expose immutable reader and writer for advanced use cases.
    private static final ObjectReader READER;
    private static final ObjectWriter WRITER;

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
    public static <T> T readValue(String json, Class<T> clazz) {
        return MAPPER.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T readValue(String json, TypeReference<T> type) {
        return MAPPER.readValue(json, type);
    }

    @SneakyThrows
    public static <T> T readValue(String json) {
        return readValue(json, Type.refer());
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

    @NonNull
    @SneakyThrows
    public static String toJson(Object data) {
        if (data == null)
            return "";

        return MAPPER.writeValueAsString(data).replace("%", "%%");
    }

    @SneakyThrows
    public static String toString(Object data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (InvalidDefinitionException e) {
            return data.toString();
        } catch (JsonProcessingException e) {
            log.error("Error occurred while parsing json", e);
            return MAPPER.writeValueAsString(e);
        }
    }

    @SneakyThrows
    public static JsonNode readTree(String data) {
        return MAPPER.readTree(data);
    }

    @SneakyThrows
    public static <T> List<T> toList(Object data) {
        return MAPPER.readValue(data.toString(), List.class);
    }

    @SneakyThrows
    public static <T, R> Map<T, R> toMap(Object data) {
        return MAPPER.readValue(data.toString(), Map.class);
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
}
