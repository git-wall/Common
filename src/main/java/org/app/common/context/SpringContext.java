package org.app.common.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        Assert.notNull(context, "Spring context not initialized");
        return context;
    }

    public static <T> T getBean(@NonNull Class<T> clazz) {
        Assert.notNull(context, "Spring context not initialized");
        return context.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return context.containsBean(name) ? getBean(clazz) : null;
    }

    public static boolean containsBean(String name) {
        return context.containsBean(name);
    }
}
