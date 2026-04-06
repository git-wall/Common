package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingUtils {

    public static <T> T mapIfNotNull(Object source, Function<Object, T> mapper) {
        return source == null ? null : mapper.apply(source);
    }
}
