package org.app.common.graphql.exception;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import lombok.RequiredArgsConstructor;
import org.app.common.support.Verify;
import org.app.common.utils.ArrayUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class GraphQLExceptionHandlerScanner {

    private final ApplicationContext context;
    private final GraphQLExceptionRegistry registry;

    @PostConstruct
    public void scan() {
        Map<String, Object> advices = context.getBeansWithAnnotation(GraphQLAdvice.class);

        for (Object bean : advices.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            for (Method method : targetClass.getMethods()) {
                GraphQLExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, GraphQLExceptionHandler.class);

                if (ann != null) {
                    validateMethod(method, ann);
                    registry.register(ann.value(), buildInvoker(bean, method));
                }
            }
        }
    }

    private void validateMethod(Method method, GraphQLExceptionHandler ann) {
        Class<?>[] params = method.getParameterTypes();
        Verify.ifTrue(ArrayUtils.isNotBetween(params, 1, 2),
            "@GraphQLExceptionHandler method must have 1 or 2 params: " + method);
        Verify.ifTrue(!Throwable.class.isAssignableFrom(params[0]),
            "First param must be Throwable: " + method);
        Verify.ifTrue(!ann.value().isAssignableFrom(params[0]),
            "Exception type mismatch with annotation: " + method);
        Verify.ifTrue(params.length == 2 && params[1] != DataFetcherExceptionHandlerParameters.class,
            "Second param (if present) must be DataFetcherExceptionHandlerParameters: " + method);
        Verify.ifTrue(!GraphQLError.class.isAssignableFrom(method.getReturnType()),
            "Return type must be GraphQLError: " + method);
    }

    private BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError> buildInvoker(Object bean, Method method) {
        return (ex, params) -> {
            try {
                return (GraphQLError) method.invoke(bean, ex, params);
            } catch (Exception e) {
                throw new RuntimeException("Error invoking GraphQL exception handler", e);
            }
        };
    }
}
