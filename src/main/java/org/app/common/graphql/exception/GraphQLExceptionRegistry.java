package org.app.common.graphql.exception;


import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Getter
@Component
public class GraphQLExceptionRegistry {

    private final Map<Class<? extends Throwable>,
        BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError>> handlers = new HashMap<>(8);

    public void register(
        Class<? extends Throwable> type,
        BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError> fn
    ) {
        handlers.put(type, fn);
    }

    public BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError> get(Class<?> type) {
        return handlers.get(type);
    }

}
