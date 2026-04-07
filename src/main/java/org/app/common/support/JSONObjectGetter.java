package org.app.common.support;

import com.fasterxml.jackson.databind.JavaType;
import org.app.common.utils.JacksonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JSONObjectGetter {
    /**
     * Get values associated with the provided key in the given JSONObject instance
     *
     * @param jsonObject JSONObject instance in which to search the key
     * @param key        Key we're interested in
     * @return List of values associated with the given key, in the order of appearance.
     * If the key is absent, an empty list is returned.
     */
    public static List<String> getValuesInObject(JSONObject jsonObject, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (String currentKey : jsonObject.keySet()) {
            Object value = jsonObject.get(currentKey);
            if (currentKey.equals(key)) {
                accumulatedValues.add(value.toString());
            }

            if (value instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInObject((JSONObject) value, key));
            } else if (value instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInArray((JSONArray) value, key));
            }
        }

        return accumulatedValues;
    }

    /**
     * Get values associated with the provided key in the given JSONArray instance
     *
     * @param jsonArray JSONArray instance in which to search the key
     * @param key       Key we're interested in
     * @return List of values associated with the given key, in the order of appearance.
     * If the key is absent, an empty list is returned.
     */
    public static List<String> getValuesInArray(JSONArray jsonArray, String key) {
        List<String> accumulatedValues = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray) {
                accumulatedValues.addAll(getValuesInArray((JSONArray) obj, key));
            } else if (obj instanceof JSONObject) {
                accumulatedValues.addAll(getValuesInObject((JSONObject) obj, key));
            }
        }

        return accumulatedValues;
    }

    /**
     * Among all the values associated with the given key, get the N-th value
     *
     * @param jsonObject JSONObject instance in which to search the key
     * @param key        Key we're interested in
     * @param N          Index of the value to get
     * @return N-th value associated with the key, or null if the key is absent or
     * the number of values associated with the key is less than N
     */
    public static String getNthValue(JSONObject jsonObject, String key, int N) {
        List<String> values = getValuesInObject(jsonObject, key);
        return (values.size() >= N) ? values.get(N - 1) : null;
    }

    /**
     * Count the number of values associated with the given key
     *
     * @param jsonObject JSONObject instance in which to count the key
     * @param key        Key we're interested in
     * @return The number of values associated with the given key
     */
    public static int getCount(JSONObject jsonObject, String key) {
        List<String> values = getValuesInObject(jsonObject, key);
        return values.size();
    }

    public static List<Map<String, Object>> convertToListObj(JSONArray jsonArray, String... keys) {
        return IntStream.range(0, jsonArray.length())
            .mapToObj(i -> convertToMapObj(jsonArray, i, keys))
            .collect(Collectors.toList());
    }

    public static <T> List<T> convertToListObj(JSONArray jsonArray, Class<T> clazz, String... keys) {
        return IntStream.range(0, jsonArray.length())
            .mapToObj(i -> convertToMapObj(jsonArray, i, clazz, keys))
            .collect(Collectors.toList());
    }

    public static <T> List<T> convertToListObj(JSONArray jsonArray, JavaType type, String... keys) {
        return IntStream.range(0, jsonArray.length())
            .<T>mapToObj(i -> convertToMapObj(jsonArray, i, type, keys))
            .collect(Collectors.toList());
    }

    public static Map<String, Object> convertToMapObj(JSONArray jsonArray, int i, String... keys) {
        Map<String, Object> accumulatedValue = new HashMap<>(keys.length);
        for (String key : keys) {
            accumulatedValue.put(key, jsonArray.getJSONObject(i).get(key));
        }
        return accumulatedValue;
    }

    public static <T> T convertToMapObj(JSONArray jsonArray, int i, Class<T> clazz, String... keys) {
        Map<String, Object> accumulatedValue = convertToMapObj(jsonArray, i, keys);
        return JacksonUtils.convert(accumulatedValue, clazz);
    }

    public static <T> T convertToMapObj(JSONArray jsonArray, int i, JavaType type, String... keys) {
        Map<String, Object> accumulatedValue = convertToMapObj(jsonArray, i, keys);
        return JacksonUtils.convert(accumulatedValue, type);
    }

    public static <T> List<T> getValuesInObjectAsType(JSONArray jsonArray, String key, Class<T> clazz) {
        List<T> accumulatedValues = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            T value = clazz.cast(jsonArray.getJSONObject(i).get(key));
            accumulatedValues.add(value);
        }

        return accumulatedValues;
    }
}
