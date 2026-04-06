package org.app.common.module.benchmark.core;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AppPackageResolver implements ApplicationContextAware {

    private static String basePackage;

    public static String basePackage() {
        return basePackage;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Object app = applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
            .values()
            .iterator()
            .next();

        basePackage = app.getClass().getPackageName();

        System.out.println(">>> BASE PACKAGE = " + basePackage);
    }
}
