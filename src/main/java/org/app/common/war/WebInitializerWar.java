package org.app.common.war;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class WebInitializerWar extends SpringBootServletInitializer {

    @Value("${app.main.class}")
    private String mainClassName;

    @Value("${servlet.initializer}")
    private boolean enableServletInitializer;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        if (!enableServletInitializer) {
            return builder;
        }

        try {
            if (!StringUtils.isEmpty(mainClassName)) {
                Class<?> mainClass = Class.forName(mainClassName);
                return builder.sources(mainClass);
            }

            return builder;
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load main application class: " + mainClassName, e);
            return builder;
        }
    }
}
