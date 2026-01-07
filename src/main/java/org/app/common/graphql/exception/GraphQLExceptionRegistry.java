package org.app.common.graphql.exception;


import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Getter
@Component
public class GraphQLExceptionRegistry {


    private final Map<Class<? extends Throwable>,
        BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError>>
        handlers = new ConcurrentHashMap<>(8);

    public void register(
        Class<? extends Throwable> type,
        BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError> fn
    ) {
        if (handlers.containsKey(type)) {
            throw new IllegalStateException(
                "Duplicate @GraphQLExceptionHandler for exception: " + type.getName()
            );
        }
        handlers.put(type, fn);
    }

    public BiFunction<Throwable, DataFetcherExceptionHandlerParameters, GraphQLError>
    get(Class<? extends Throwable> type) {
        return handlers.get(type);
    }
}
