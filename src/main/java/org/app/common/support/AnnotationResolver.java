package org.app.common.support;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationResolver {
    /**
     * Finds the annotation on the most specific method implementation, including proxy resolution.
     */
    public static <T extends Annotation> T findMethodAnnotation(JoinPoint joinPoint, Class<T> annotationType) {
        Method method = resolveMethod(joinPoint);
        return AnnotationUtils.findAnnotation(method, annotationType);
    }

    /**
     * Finds the merged annotation (including meta/composed annotations).
     */
    public static <T extends Annotation> T findMergedMethodAnnotation(JoinPoint joinPoint, Class<T> annotationType) {
        Method method = resolveMethod(joinPoint);
        return AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
    }

    /**
     * Finds the annotation on the target class.
     */
    public static <T extends Annotation> T findClassAnnotation(JoinPoint joinPoint, Class<T> annotationType) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return AnnotationUtils.findAnnotation(targetClass, annotationType);
    }

    /**
     * Finds the merged annotation (including meta-annotations) on the target class.
     */
    public static <T extends Annotation> T findMergedClassAnnotation(JoinPoint joinPoint, Class<T> annotationType) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, annotationType);
    }

    /**
     * Resolves the actual implementation method from an interface method.
     */
    private static Method resolveMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method interfaceMethod = signature.getMethod();
        return AopUtils.getMostSpecificMethod(interfaceMethod, joinPoint.getTarget().getClass());
    }
}
