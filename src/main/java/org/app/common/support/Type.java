package org.app.common.support;

import com.fasterxml.jackson.core.type.TypeReference;

public class Type {
    public static <T> TypeReference<T> refer() {
        return new TypeReference<>() {
        };
    }
}
