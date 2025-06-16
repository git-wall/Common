package org.app.common.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.ParameterizedTypeReference;

public class Type {
    public static <T> TypeReference<T> refer() {
        return new TypeReference<>() {
        };
    }

    public static <T> ParameterizedTypeReference<T> paramRefer() {
        return new ParameterizedTypeReference<>() {
        };
    }
}
