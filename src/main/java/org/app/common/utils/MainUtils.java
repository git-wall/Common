package org.app.common.utils;

import lombok.NoArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;

@NoArgsConstructor
public class MainUtils {

    public static void runSpringAppWithBannerOff(Class<?> clazz, String[] args) {
        SpringApplication app = new SpringApplication(clazz);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
