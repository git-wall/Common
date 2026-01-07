package org.app.common.utils;

public class JavaVersionUtils {
    public static boolean isJava8() {
        return System.getProperty("java.specification.version").startsWith("1.");
    }
}
