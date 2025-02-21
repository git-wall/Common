package org.app.common.utils;

import java.util.Properties;

public class ConfigUtils {
    private ConfigUtils() {
        // Private constructor to prevent instantiation
    }

    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (var inputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties file: " + filePath, e);
        }
        return properties;
    }
}
