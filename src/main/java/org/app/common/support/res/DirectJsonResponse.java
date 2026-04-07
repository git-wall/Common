package org.app.common.support.res;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class DirectJsonResponse {

    private final JsonFactory jsonFactory = new JsonFactory();

    /**
     * Write single object response
     */
    public void writeObject(HttpServletResponse response,
                            ObjectWriter writer) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (JsonGenerator generator = jsonFactory.createGenerator(response.getOutputStream())) {
            JsonWriter json = new JsonWriter(generator);
            json.writeStartObject();
            writer.write(json);
            json.writeEndObject();
        }
    }

    /**
     * Write array response
     */
    public <T> void writeArray(HttpServletResponse response,
                               List<T> items,
                               ItemWriter<T> writer) throws IOException {
        response.setContentType("application/json");

        try (JsonGenerator generator = jsonFactory.createGenerator(response.getOutputStream())) {
            JsonWriter json = new JsonWriter(generator);
            json.writeStartArray();
            for (T item : items) {
                json.writeStartObject();
                writer.write(json, item);
                json.writeEndObject();
            }
            json.writeEndArray();
        }
    }
}
