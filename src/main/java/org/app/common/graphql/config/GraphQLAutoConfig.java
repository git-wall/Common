package org.app.common.graphql.config;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.RequiredArgsConstructor;
import org.app.common.context.SpringContext;
import org.app.common.graphql.exception.GlobalGraphQLExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class GraphQLAutoConfig {

    private final List<WiringModule> modules; // Spring auto-inject

    public GraphQL graphQL;

    @PostConstruct
    public void init() throws IOException {
        TypeDefinitionRegistry registry = loadSchema();
        RuntimeWiring wiring = buildWiring();

        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);

        var exceptionHandler = SpringContext.getBean(GlobalGraphQLExceptionHandler.class);

        this.graphQL = GraphQL.newGraphQL(schema)
            .defaultDataFetcherExceptionHandler(exceptionHandler)
            .build();
    }

    private TypeDefinitionRegistry loadSchema() throws IOException {
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        SchemaParser parser = new SchemaParser();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:graphql");

        for (Resource resource : resources) {
            Reader reader = new InputStreamReader(resource.getInputStream());
            typeRegistry.merge(parser.parse(reader));
        }
        return typeRegistry;
    }

    private RuntimeWiring buildWiring() {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        modules.forEach(m -> m.apply(builder)); // merge module hook
        return builder.build();
    }
}
