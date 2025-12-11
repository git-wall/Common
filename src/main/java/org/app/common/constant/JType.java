package org.app.common.constant;

import com.fasterxml.jackson.databind.JavaType;
import org.app.common.utils.JacksonUtils;

import java.util.List;
import java.util.Map;

public class JType {

    private JType() {
    }

    // Common JavaType definitions for use with Jackson serialization/deserialization

    public static final JavaType VOID = JacksonUtils.typeOf(Void.class);

    public static final JavaType OBJ = JacksonUtils.typeOf(Object.class);

    // List<String>
    public static final JavaType LIST_STR = JacksonUtils.listType(String.class);

    // List<Integer>
    public static final JavaType LIST_INT = JacksonUtils.listType(Integer.class);

    // List<Object>
    public static final JavaType LIST_OBJ = JacksonUtils.listType(Object.class);

    // List<Map<String, String>>
    public static final JavaType LIST_MAP_STR_STR = JacksonUtils.typeOf(List.class, Map.class, String.class, String.class);

    // List<Map<String, Object>>
    public static final JavaType LIST_MAP_STR_OBJ = JacksonUtils.typeOf(List.class, Map.class, String.class, Object.class);

    // Map<Integer, Object>
    public static final JavaType MAP_INT_OBJ =  JacksonUtils.mapType(Integer.class, Object.class);

    // Map<String, Object>
    public static final JavaType MAP_STR_OBJ = JacksonUtils.mapType(String.class, Object.class);

    // Map<String, List<Object>>
    public static final JavaType MAP_STR_LIST_OBJ = JacksonUtils.typeOf(Map.class, String.class, List.class, Object.class);

    // Map<String, Map<String, Object>>
    public static final JavaType MAP_STR_MAP_STR_OBJ = JacksonUtils.typeOf(Map.class, String.class, Map.class, String.class, Object.class);
}
