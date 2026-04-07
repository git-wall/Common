package org.app.jackson;

import com.fasterxml.jackson.databind.JavaType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/// Common JavaType definitions for use with Jackson serialization/deserialization
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class JType {
    public static final JavaType VOID = JacksonUtils.typeOf(Void.class);
    public static final JavaType STRING = JacksonUtils.typeOf(String.class);
    public static final JavaType INTEGER = JacksonUtils.typeOf(Integer.class);
    public static final JavaType LONG = JacksonUtils.typeOf(Long.class);
    public static final JavaType OBJECT = JacksonUtils.typeOf(Object.class);

    /// ===== Simple Collections =====
    public static final JavaType LIST_STR = JacksonUtils.listType(String.class);
    public static final JavaType LIST_INT = JacksonUtils.listType(Integer.class);
    public static final JavaType LIST_LONG = JacksonUtils.listType(Long.class);
    public static final JavaType LIST_OBJ = JacksonUtils.listType(Object.class);


    /// ===== Simple Maps =====
    public static final JavaType MAP_STR_STR = JacksonUtils.mapType(String.class, Object.class);
    public static final JavaType MAP_STR_OBJ = JacksonUtils.mapType(String.class, Object.class);
    public static final JavaType MAP_INT_OBJ = JacksonUtils.mapType(Integer.class, Object.class);


    /// ===== Complex Nested Types =====
    // List<Map<String, Object>>
    public static final JavaType LIST_MAP_STR_OBJ = JacksonUtils.typeOf(List.class, MAP_STR_OBJ);
    // Map<String, List<String>>
    public static final JavaType MAP_STR_LIST_STR = JacksonUtils.mapType(JacksonUtils.typeOf(String.class), LIST_STR);
    // Map<String, List<Object>>
    public static final JavaType MAP_STR_LIST_OBJ = JacksonUtils.mapType(JacksonUtils.typeOf(String.class), LIST_OBJ);
    // Map<String, Map<String, Object>>
    public static final JavaType MAP_STR_MAP_STR_OBJ = JacksonUtils.mapType(JacksonUtils.typeOf(String.class), MAP_STR_OBJ);
}
