package org.app.common.support.res;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.List;

public class JsonWriter {
    private final JsonGenerator generator;

    public JsonWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    // Primitive fields
    public void writeNumberField(String name, long value) throws IOException {
        generator.writeNumberField(name, value);
    }

    public void writeNumberField(String name, double value) throws IOException {
        generator.writeNumberField(name, value);
    }

    public void writeStringField(String name, String value) throws IOException {
        generator.writeStringField(name, value);
    }

    public void writeBooleanField(String name, boolean value) throws IOException {
        generator.writeBooleanField(name, value);
    }

    // Nested object
    public void writeObject(String name, ObjectWriter writer) throws IOException {
        generator.writeObjectFieldStart(name);
        writer.write(this);
        generator.writeEndObject();
    }

    // Array
    public <T> void writeArray(String name, List<T> items, ItemWriter<T> writer) throws IOException {
        generator.writeArrayFieldStart(name);
        for (T item : items) {
            generator.writeStartObject();
            writer.write(this, item);
            generator.writeEndObject();
        }
        generator.writeEndArray();
    }

    // Internal use
    void writeStartObject() throws IOException {
        generator.writeStartObject();
    }

    void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    void writeEndArray() throws IOException {
        generator.writeEndArray();
    }
}
