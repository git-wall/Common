package org.app.common.utils;

import org.springframework.context.ApplicationContext;

public class EnvironmentUtils {
    public static String getPropertyString(ApplicationContext applicationContext, String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    public static Boolean getPropertyBoolean(ApplicationContext applicationContext, String key) {
        String temp = getPropertyString(applicationContext, key);
        return temp != null ? Boolean.valueOf(temp) : null;
    }
}
