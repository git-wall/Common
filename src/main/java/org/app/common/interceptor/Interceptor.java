package org.app.common.interceptor;

import org.app.common.support.AnnotationResolver;
import org.aspectj.lang.JoinPoint;

import java.util.function.Supplier;

public class Interceptor<T extends java.lang.annotation.Annotation> {
    public T getOrDefault(JoinPoint joinPoint, Class<T> type, Supplier<T> supplier) {
        T annotation = AnnotationResolver.findClassAnnotation(joinPoint, type);
        if (annotation != null) {
            return annotation;
        }
        return supplier.get();
    }
}
