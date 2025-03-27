package org.app.common.utils;

import com.google.gson.Gson;

public class GsonUtils {
    private static final Gson GSON;

    static {
        GSON = new Gson();
    }

    public static Gson gson() {
        return GSON;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static String toJsonString(Object object) {
        return GSON.toJson(object);
    }
}
