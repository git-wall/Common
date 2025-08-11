package org.app.common.support;

import com.fasterxml.jackson.databind.JavaType;
import org.app.common.utils.JacksonUtils;

import java.util.List;
import java.util.Map;

public class JType {

    // Common JavaType definitions for use with Jackson serialization/deserialization

    // List<String>
    public static final JavaType LIST_STRING = JacksonUtils.typeOf(List.class, String.class);

    // List<Integer>
    public static final JavaType LIST_INTEGER = JacksonUtils.typeOf(List.class, Integer.class);

    // List<Object>
    public static final JavaType LIST_OBJECT = JacksonUtils.typeOf(List.class, Object.class);

    // List<Map<String, Object>>
    public static final JavaType LIST_MAP_STRING_OBJECT = JacksonUtils.typeOf(List.class, Map.class, String.class, Object.class);

    // Map<String, Object>
    public static final JavaType MAP_STRING_OBJECT = JacksonUtils.typeOf(Map.class, String.class, Object.class);

    // Map<String, List<Object>>
    public static final JavaType MAP_STRING_LIST_OBJECT = JacksonUtils.typeOf(Map.class, String.class, List.class, Object.class);

    // Map<String, Map<String, Object>>
    public static final JavaType MAP_STRING_MAP_STRING_OBJECT = JacksonUtils.typeOf(Map.class, String.class, Map.class, String.class, Object.class);
}
