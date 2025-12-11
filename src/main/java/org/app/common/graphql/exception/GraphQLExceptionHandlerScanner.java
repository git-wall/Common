package org.app.common.graphql.exception;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

@Component
public class GraphQLExceptionHandlerScanner {

    public GraphQLExceptionHandlerScanner(ApplicationContext context, GraphQLExceptionRegistry registry) {
        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanName);
            Method[] methods = bean.getClass().getMethods();

            for (Method m : methods) {
                GraphQLExceptionHandler ann = m.getAnnotation(GraphQLExceptionHandler.class);

                if (ann != null) {
                    Class<? extends Throwable> exceptionType = ann.value();

                    registry.register(exceptionType, buildInvoker(bean, m));
                }
            }
        }
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
