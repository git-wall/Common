package org.app.common.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

public class TokenUtils {

    @NonNull
    public static String generateId(String prefix, int length) {
        String pre = StringUtils.hasText(prefix)
                ? prefix + "_"
                : "";
        return pre + RandomStringUtils.randomAlphanumeric(length).toLowerCase();
    }
}
