package org.app.common.module.benchmark.core;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.aggregate.ProfileAggregator;
import org.app.common.module.benchmark.annotation.ApiProfiled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ektorp.util.Assert;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ProfilerInterceptor {

    @Around("@annotation(api)")
    public Object around(ProceedingJoinPoint pjp, ApiProfiled api) throws Throwable {
        String apiName = api.value();
        Assert.hasText(apiName, "ApiProfiled.value() is empty need the name");

        ProfilerContext.enter(apiName);
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
