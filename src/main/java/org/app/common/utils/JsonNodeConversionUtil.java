package org.app.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonNodeConversionUtil {
    static <T> List<T> manualJsonNodeToList(JsonNode arrayNode, JsonNodeToObjectConverter<T> converter) {
        List<T> items = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            items.add(converter.convert(node));
        }
        return items;
    }

    static <T> Map<String, T> manualJsonNodeToMap(JsonNode objectNode, JsonNodeToObjectConverter<T> converter) {
        Map<String, T> map = new HashMap<>();
        objectNode.fields()
                .forEachRemaining(node -> map.put(node.getKey(), converter.convert(node.getValue())));
        return map;
    }

    static <T> List<T> readValueJsonNodeToList(JsonNode arrayNode, TypeReference<List<T>> typeReference) throws IOException {
        return new ObjectMapper().readValue(arrayNode.traverse(), typeReference);
    }

    static <T> Map<String, T> readValueJsonNodeToMap(JsonNode objectNode, TypeReference<Map<String, T>> typeReference) throws IOException {
        return new ObjectMapper().readValue(objectNode.traverse(), typeReference);
    }

    static <T> List<T> convertValueJsonNodeToList(JsonNode arrayNode, TypeReference<List<T>> typeReference) {
        return new ObjectMapper().convertValue(arrayNode, typeReference);
    }

    static <T> Map<String, T> convertValueJsonNodeToMap(JsonNode objectNode, TypeReference<Map<String, T>> typeReference) {
        return new ObjectMapper().convertValue(objectNode, typeReference);
    }

    @FunctionalInterface
    interface JsonNodeToObjectConverter<T> {
        T convert(JsonNode node);
    }
}
