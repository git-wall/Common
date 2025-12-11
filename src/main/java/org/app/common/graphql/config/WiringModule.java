package org.app.common.graphql.config;

import graphql.schema.idl.RuntimeWiring;

public interface WiringModule {
    void apply(RuntimeWiring.Builder builder);
}
