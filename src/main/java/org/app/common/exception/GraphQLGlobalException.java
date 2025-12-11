package org.app.common.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import org.app.common.graphql.exception.GraphQLExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class GraphQLGlobalException {

    @GraphQLExceptionHandler(NotFoundException.class)
    public GraphQLError handleNotFound(Throwable ex, DataFetcherExceptionHandlerParameters params) {
        return GraphqlErrorBuilder.newError()
            .message("NOT FOUND: " + ex.getMessage())
            .path(params.getPath())
            .location(params.getSourceLocation())
            .build();
    }
}
