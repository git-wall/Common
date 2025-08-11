package org.app.common.agent.transform;

import net.bytebuddy.implementation.bind.annotation.*;
import org.app.common.agent.dispatch.TraceDispatcher;
import org.app.common.context.TracingContext;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class TraceTransformer {
    private final TraceDispatcher dispatcher;

    public TraceTransformer(TraceDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RuntimeType
    public Object intercept(@This Object obj,
                          @Origin Method method,
                          @SuperCall Callable<?> callable,
                          @AllArguments Object[] args) throws Exception {
        String traceId = TracingContext.getRequestId();
        long startTime = System.currentTimeMillis();

        try {
            Object result = callable.call();
            recordTrace(obj, method, args, result, startTime, null);
            return result;
        } catch (Exception e) {
            recordTrace(obj, method, args, null, startTime, e);
            throw e;
        }
    }

    private void recordTrace(Object target, Method method, Object[] args,
                           Object result, long startTime, Exception error) {
        try {
            TracingContext.put("className", target.getClass().getName());
            TracingContext.put("methodName", method.getName());
            TracingContext.put("args", args);
            TracingContext.put("duration", System.currentTimeMillis() - startTime);

            if (error != null) {
                TracingContext.put("error", error.getMessage());
                TracingContext.put("errorType", error.getClass().getName());
            } else if (result != null) {
                TracingContext.put("result", result.toString());
            }

            dispatcher.dispatch(TracingContext.getRequestId(), TracingContext.getContext());
        } finally {
            TracingContext.clear();
        }
    }
}
