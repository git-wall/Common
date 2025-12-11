package org.app.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonNodeConversionUtil {
    static <T> List<T> convertToList(JsonNode arrayNode, JsonNodeToObjectConverter<T> converter) {
        List<T> items = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            items.add(converter.convert(node));
        }
        return items;
    }

    static <T> Map<String, T> convertToMap(JsonNode objectNode, JsonNodeToObjectConverter<T> converter) {
        Map<String, T> map = new HashMap<>();
        objectNode.properties()
            .forEach(node -> map.put(node.getKey(), converter.convert(node.getValue())));
        return map;
    }

    static <T> List<T> readToList(JsonNode arrayNode, TypeReference<List<T>> typeReference) throws IOException {
        return JacksonUtils.mapper().readValue(arrayNode.toString(), typeReference);
    }

    static <T> Map<String, T> readToMap(JsonNode objectNode, TypeReference<Map<String, T>> typeReference) throws IOException {
        return JacksonUtils.mapper().readValue(objectNode.traverse(), typeReference);
    }

    static <T> List<T> convertToList(JsonNode arrayNode, TypeReference<List<T>> typeReference) {
        return JacksonUtils.mapper().convertValue(arrayNode, typeReference);
    }

    static <T> Map<String, T> convertToMap(JsonNode objectNode, TypeReference<Map<String, T>> typeReference) {
        return JacksonUtils.mapper().convertValue(objectNode, typeReference);
    }

    @FunctionalInterface
    interface JsonNodeToObjectConverter<T> {
        T convert(JsonNode node);
    }
}
