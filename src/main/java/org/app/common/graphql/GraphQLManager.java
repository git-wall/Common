package org.app.common.graphql;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class GraphQLManager {

    private final Map<Type, UnaryOperator<TypeRuntimeWiring.Builder>> typeWiringBuilders;

    public GraphQLManager() {
        this.typeWiringBuilders = new HashMap<>();
    }

    public GraphQLManager newInstance() {
        return new GraphQLManager();
    }

    public GraphQLManager register(Type type, UnaryOperator<TypeRuntimeWiring.Builder> builder) {
        this.typeWiringBuilders.put(type, builder);
        return this;
    }

    public GraphQLManager registerQuery(UnaryOperator<TypeRuntimeWiring.Builder> builder) {
        this.typeWiringBuilders.put(Type.QUERY, builder);
        return this;
    }

    public GraphQLManager registerMutation(UnaryOperator<TypeRuntimeWiring.Builder> builder) {
        this.typeWiringBuilders.put(Type.MUTATION, builder);
        return this;
    }

    public GraphQLManager registerSubscription(UnaryOperator<TypeRuntimeWiring.Builder> builder) {
        this.typeWiringBuilders.put(Type.SUBSCRIPTION, builder);
        return this;
    }

    public RuntimeWiring buildWiring() {
        RuntimeWiring.Builder rwb = typeWiringBuilder();
        return rwb.build();
    }

    private RuntimeWiring.Builder typeWiringBuilder() {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        typeWiringBuilders.forEach((type, unaryOperator) -> {
            TypeRuntimeWiring.Builder typeBuilder = TypeRuntimeWiring.newTypeWiring(type.name());
            unaryOperator.apply(typeBuilder);
            builder.type(typeBuilder);
        });
        return builder;
    }

    public enum Type {
        QUERY,
        MUTATION,
        SUBSCRIPTION
    }
}
