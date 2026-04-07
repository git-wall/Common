package org.app.common.module.benchmark.core;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.aggregate.ProfileAggregator;
import org.app.common.module.benchmark.annotation.ApiProfiled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(1)
public class ProfilerInterceptor {

    @Around("@annotation(api)")
    public Object around(ProceedingJoinPoint pjp, ApiProfiled api) throws Throwable {

        String apiName = api.value();
        if (apiName.isEmpty()) {
            apiName = pjp.getSignature().toShortString();
            log.warn("ApiProfiled.value() is empty, using method signature: {}", apiName);
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();
        String fullMethodName = className + "." + methodName;

        ProfilerContext.enter(fullMethodName);

        try {
            return pjp.proceed();

        } catch (Throwable ex) {
            log.error("Error in profiled API: {}", apiName, ex);
            throw ex;

        } finally {
            TraceNode root = ProfilerContext.finish();

            if (root != null) {
                TreePrinter.print(root, api);
                ProfileAggregator.accept(apiName, root);
            }
        }
    }
}
