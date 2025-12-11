package org.app.common.graphql.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class GlobalGraphQLExceptionHandler implements DataFetcherExceptionHandler {

    private final GraphQLExceptionRegistry registry;

    public GlobalGraphQLExceptionHandler(GraphQLExceptionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
        DataFetcherExceptionHandlerParameters params) {

        Throwable ex = params.getException();
        var handler = registry.get(ex.getClass());

        GraphQLError error = (handler != null)
            ? handler.apply(ex, params)
            : defaultHandler(ex, params);

        return CompletableFuture.completedFuture(
            DataFetcherExceptionHandlerResult.newResult().error(error).build()
        );
    }

    private GraphQLError defaultHandler(Throwable ex, DataFetcherExceptionHandlerParameters params) {
        return GraphqlErrorBuilder.newError()
            .message("INTERNAL ERROR: " + ex.getMessage())
            .errorType(ErrorType.DataFetchingException)
            .path(params.getPath())
            .location(params.getSourceLocation())
            .build();
    }
}
