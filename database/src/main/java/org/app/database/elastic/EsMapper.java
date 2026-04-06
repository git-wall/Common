package org.app.database.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * Shared Jackson mapper for converting ES source maps ↔ domain objects.
 * A single instance is shared across all EsOps / EsSearch instances.
 */
final class EsMapper {

    private final ObjectMapper mapper;

    EsMapper() {
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    EsMapper(ObjectMapper custom) {
        this.mapper = custom;
    }

    @SuppressWarnings("unchecked")
    <T> Map<String, Object> toMap(T doc) {
        return mapper.convertValue(doc, Map.class);
    }

    <T> T fromMap(Map<String, Object> source, Class<T> type) {
        return mapper.convertValue(source, type);
    }

    ObjectMapper raw() { return mapper; }
}
