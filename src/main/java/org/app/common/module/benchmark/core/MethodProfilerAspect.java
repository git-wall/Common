package org.app.common.module.benchmark.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.lang.reflect.Method;

@Aspect
@Component
@Order(2)
public class MethodProfilerAspect {

    @Around(
        "execution(public * org.app..*(..)) " +
            "&& !@annotation(org.app.common.module.benchmark.annotation.ApiProfiled) " +
            "&& !execution(* org.app.common.module.benchmark..*(..))"
    )
    public Object profileMethod(ProceedingJoinPoint pjp) throws Throwable {
        if (!ProfilerContext.isActive()) {
            return pjp.proceed();
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = signature.getDeclaringType();

        String pkg = clazz.getPackageName();

        if (!pkg.startsWith(AppPackageResolver.basePackage())) {
            return pjp.proceed();
        }

        if (isGetterSetter(method) || isNoiseClass(clazz)) {
            return pjp.proceed();
        }

        String fullName = clazz.getSimpleName() + "." + method.getName();

        ProfilerContext.enter(fullName);
        try {
            return pjp.proceed();
        } finally {
            ProfilerContext.exit();
        }
    }

    private boolean isGetterSetter(Method m) {
        return (m.getName().startsWith("get") && m.getParameterCount() == 0)
            || (m.getName().startsWith("set") && m.getParameterCount() == 1)
            || (m.getName().startsWith("is") && m.getParameterCount() == 0);
    }

    private boolean isNoiseClass(Class<?> c) {
        return c.isAnnotationPresent(Entity.class)
            || c.isAnnotationPresent(Configuration.class)
            || c.getSimpleName().endsWith("DTO")
            || c.getSimpleName().endsWith("VO")
            || c.getSimpleName().endsWith("Util")
            || c.getSimpleName().endsWith("Mapper");
    }
}
