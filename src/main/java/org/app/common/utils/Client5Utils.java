package org.app.common.utils;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.app.common.entities.rest.RestInfo;
import org.springframework.lang.NonNull;

public class Client5Utils {

    private Client5Utils() {
        throw new IllegalStateException("Utility class");
    }

    @SneakyThrows
    public static <T> T getFieldBody(String fieldName, Class<T> fieldType, @NonNull RestInfo requestInfo) {
        return getFieldAny(fieldName, fieldType, requestInfo);
    }

    @SneakyThrows
    public static <T> T getFieldBody(String field, @NonNull RestInfo restInfo) {
        @SuppressWarnings("unchecked")
        T t = (T) getFieldAny(field, Object.class, restInfo);
        return t;
    }

    /**
     * Retrieves a field from the response body of a POST request to the specified URI.
     *
     * @param field    the name of the field to retrieve from the response body
     * @param clazz    the expected type of the field
     * @param restInfo the request information including headers and form parameters
     * @return the value of the specified field from the response body, cast to the specified type
     */
    @SneakyThrows
    public static <T> T getFieldAny(String field, @NonNull Class<T> clazz, @NonNull RestInfo restInfo) {
        String responseBody = Request.post(restInfo.getUri())
                .setHeaders(restInfo.getHeaders())
                .bodyForm(restInfo.getFormParams())
                .execute()
                .returnContent()
                .asString();

        JsonObject jsonObject = MapperUtils.gson().fromJson(responseBody, JsonObject.class);
        return clazz.cast(jsonObject.get(field));
    }

    /**
     * Retrieves the response body of a POST request to the specified URI as a string.
     *
     * @param restInfo the request information including headers and form parameters
     * @return the response body as a string
     */
    @SneakyThrows
    public static String getBodyAsString(@NonNull RestInfo restInfo) {
        return Request.post(restInfo.getUri())
                .setHeaders(restInfo.getHeaders())
                .bodyForm(restInfo.getFormParams())
                .execute()
                .returnContent()
                .asString();
    }

    /**
     * Retrieves the response body of a POST request to the specified URI as a JSON object.
     *
     * @param restInfo the request information including headers and form parameters
     * @return the response body as a JSON object
     */
    @SneakyThrows
    public static JsonObject getBodyAsJsonObject(@NonNull RestInfo restInfo) {
        String responseBody = Request.post(restInfo.getUri())
                .setHeaders(restInfo.getHeaders())
                .bodyForm(restInfo.getFormParams())
                .execute()
                .returnContent()
                .asString();

        return MapperUtils.gson().fromJson(responseBody, JsonObject.class);
    }

    /**
     * Retrieves the response body of a POST request to the specified URI as an object of the specified class.
     *
     * @param clazz    the class of the object to be retrieved from the response body
     * @param restInfo the request information including headers and form parameters
     * @param <T>      the type of the object being retrieved
     * @return the object from the response body, cast to the specified class
     */
    @SneakyThrows
    public static <T> T getBodyAsClass(Class<T> clazz, @NonNull RestInfo restInfo) {
        String responseBody = Request.post(restInfo.getUri())
                .setHeaders(restInfo.getHeaders())
                .bodyForm(restInfo.getFormParams())
                .execute()
                .returnContent()
                .asString();

        return MapperUtils.mapper().readValue(responseBody, clazz);
    }

    @SneakyThrows
    public static ClassicHttpResponse getResponse(RestInfo restInfo) {
        return (ClassicHttpResponse) Request.post(restInfo.getUri())
                .setHeaders(restInfo.getHeaders())
                .bodyForm(restInfo.getFormParams())
                .execute()
                .returnResponse();
    }
}
