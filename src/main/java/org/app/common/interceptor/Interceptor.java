package org.app.common.interceptor;

import org.app.common.support.AnnotationResolver;
import org.aspectj.lang.JoinPoint;

public class Interceptor<T extends java.lang.annotation.Annotation> {
    public T get(JoinPoint joinPoint, Class<T> type) {
        return AnnotationResolver.findClassAnnotation(joinPoint, type);
    }
}
