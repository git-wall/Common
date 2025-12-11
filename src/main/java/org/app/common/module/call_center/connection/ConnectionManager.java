package org.app.common.module.call_center.connection;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.model.CallDefinition;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages connections to different data sources with versioning support
 */
@Slf4j
@Component
public class ConnectionManager {
    // Cache for database connections: key = definitionId, value = ConnectionInfo
    private final Map<String, ConnectionInfo<DataSource>> databaseConnections = new ConcurrentHashMap<>();

    // Cache for API clients: key = definitionId, value = ConnectionInfo
    private final Map<String, ConnectionInfo<RestTemplate>> apiConnections = new ConcurrentHashMap<>();

    // Cache for Elasticsearch clients: key = definitionId, value = ConnectionInfo
    private final Map<String, ConnectionInfo<Object>> elasticsearchConnections = new ConcurrentHashMap<>();

    // Cache for GraphQL clients: key = definitionId, value = ConnectionInfo
    private final Map<String, ConnectionInfo<RestTemplate>> graphqlConnections = new ConcurrentHashMap<>();

    /**
     * Get or create a database connection
     * @param definition The call definition
     * @return The database connection
     */
    public DataSource getDatabaseConnection(CallDefinition definition) {
        String definitionId = definition.getId();
        Integer version = definition.getVersion();

        // Check if connection exists and version matches
        ConnectionInfo<DataSource> connectionInfo = databaseConnections.get(definitionId);
        if (connectionInfo != null && Objects.equals(connectionInfo.getVersion(), version)) {
            return connectionInfo.getConnection();
        }

        // Create a new connection
        log.info("Creating new database connection for definition: {}, version: {}", definitionId, version);
        DataSource dataSource = createDatabaseConnection(definition);

        // Store in cache
        databaseConnections.put(definitionId, new ConnectionInfo<>(version, dataSource));

        return dataSource;
    }

    /**
     * Get or create an API client
     * @param definition The call definition
     * @return The API client
     */
    public RestTemplate getApiClient(CallDefinition definition) {
        String definitionId = definition.getId();
        Integer version = definition.getVersion();

        // Check if connection exists and version matches
        ConnectionInfo<RestTemplate> connectionInfo = apiConnections.get(definitionId);
        if (connectionInfo != null && Objects.equals(connectionInfo.getVersion(), version)) {
            return connectionInfo.getConnection();
        }

        // Create a new connection
        log.info("Creating new API client for definition: {}, version: {}", definitionId, version);
        RestTemplate restTemplate = createApiClient(definition);

        // Store in cache
        apiConnections.put(definitionId, new ConnectionInfo<>(version, restTemplate));

        return restTemplate;
    }

    /**
     * Get or create an Elasticsearch client
     * @param definition The call definition
     * @return The Elasticsearch client
     */
    public Object getElasticsearchClient(CallDefinition definition) {
        String definitionId = definition.getId();
        Integer version = definition.getVersion();

        // Check if connection exists and version matches
        ConnectionInfo<Object> connectionInfo = elasticsearchConnections.get(definitionId);
        if (connectionInfo != null && Objects.equals(connectionInfo.getVersion(), version)) {
            return connectionInfo.getConnection();
        }

        // Create new connection
        log.info("Creating new Elasticsearch client for definition: {}, version: {}", definitionId, version);
        Object esClient = createElasticsearchClient(definition);

        // Store in cache
        elasticsearchConnections.put(definitionId, new ConnectionInfo<>(version, esClient));

        return esClient;
    }

    /**
     * Get or create a GraphQL client
     * @param definition The call definition
     * @return The GraphQL client
     */
    public RestTemplate getGraphQLClient(CallDefinition definition) {
        String definitionId = definition.getId();
        Integer version = definition.getVersion();

        // Check if connection exists and version matches
        ConnectionInfo<RestTemplate> connectionInfo = graphqlConnections.get(definitionId);
        if (connectionInfo != null && Objects.equals(connectionInfo.getVersion(), version)) {
            return connectionInfo.getConnection();
        }

        // Create new connection
        log.info("Creating new GraphQL client for definition: {}, version: {}", definitionId, version);
        RestTemplate restTemplate = createGraphQLClient(definition);

        // Store in cache
        graphqlConnections.put(definitionId, new ConnectionInfo<>(version, restTemplate));

        return restTemplate;
    }

    /**
     * Clear all connections for a definition
     * @param definitionId The definition ID
     */
    public void clearConnections(String definitionId) {
        databaseConnections.remove(definitionId);
        apiConnections.remove(definitionId);
        elasticsearchConnections.remove(definitionId);
        graphqlConnections.remove(definitionId);
        log.info("Cleared all connections for definition: {}", definitionId);
    }

    /**
     * Clear all connections
     */
    public void clearAllConnections() {
        databaseConnections.clear();
        apiConnections.clear();
        elasticsearchConnections.clear();
        graphqlConnections.clear();
        log.info("Cleared all connections");
    }

    // Private methods to create connections

    private DataSource createDatabaseConnection(CallDefinition definition) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(definition.getRepository().getUrl());
        dataSource.setUsername(definition.getRepository().getUser());
        dataSource.setPassword(definition.getRepository().getPwd());
        return dataSource;
    }

    private RestTemplate createApiClient(CallDefinition definition) {
        // In a real implementation, you might configure the RestTemplate with interceptors, etc.
        return new RestTemplate();
    }

    private Object createElasticsearchClient(CallDefinition definition) {
        // In a real implementation, you would create an Elasticsearch client
        // For now, we'll just return a RestTemplate
        return new RestTemplate();
    }

    private RestTemplate createGraphQLClient(CallDefinition definition) {
        // In a real implementation, you might configure the RestTemplate with interceptors, etc.
        return new RestTemplate();
    }
}
