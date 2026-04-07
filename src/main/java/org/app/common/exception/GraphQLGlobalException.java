package org.app.common.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.app.common.exception.business.NotFoundException;
import org.app.common.graphql.exception.GraphQLAdvice;
import org.app.common.graphql.exception.GraphQLExceptionHandler;

@GraphQLAdvice
public class GraphQLGlobalException {

    @GraphQLExceptionHandler(NotFoundException.class)
    public GraphQLError handleNotFound(NotFoundException ex) {
        return GraphqlErrorBuilder.newError()
            .message(ex.getMessage())
            .build();
    }
}
