package org.app.common.constant;

import org.app.common.utils.GsonUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GType {
    private GType() {
    }


    // Common Type definitions for use with Jackson serialization/deserialization

    public static final Type VOID = GsonUtils.typeOf(Void.class);

    public static final Type OBJECT = GsonUtils.typeOf(Object.class);

    // List<String>
    public static final Type LIST_STRING = GsonUtils.typeOf(List.class, String.class);

    // List<Integer>
    public static final Type LIST_INTEGER = GsonUtils.typeOf(List.class, Integer.class);

    // List<Object>
    public static final Type LIST_OBJECT = GsonUtils.typeOf(List.class, Object.class);

    // List<Map<String, String>>
    public static final Type LIST_MAP_STRING_STRING = GsonUtils.typeOf(List.class, Map.class, String.class, String.class);

    // List<Map<String, Object>>
    public static final Type LIST_MAP_STRING_OBJECT = GsonUtils.typeOf(List.class, Map.class, String.class, Object.class);

    // Map<String, Object>
    public static final Type MAP_STRING_OBJECT = GsonUtils.typeOf(Map.class, String.class, Object.class);

    // Map<String, List<Object>>
    public static final Type MAP_STRING_LIST_OBJECT = GsonUtils.typeOf(Map.class, String.class, List.class, Object.class);

    // Map<String, Map<String, Object>>
    public static final Type MAP_STRING_MAP_STRING_OBJECT = GsonUtils.typeOf(Map.class, String.class, Map.class, String.class, Object.class);
}
