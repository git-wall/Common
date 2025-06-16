package org.app.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
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
