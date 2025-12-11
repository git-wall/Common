package org.app.common.module.call_center.connector.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connection.ConnectionManager;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConnector implements Connector {
    private final ConnectionManager connectionManager;

    @Override
    public ResultSet execute(CallDefinition definition, Map<String, Object> inputs) {
        try {
            // Get connection from connection manager (with versioning support)
            DataSource dataSource = connectionManager.getDatabaseConnection(definition);
            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            
            // Get query from definition
            String query = definition.getCall().getQuery();
            
            // Apply LIMIT if specified in options
            Integer limit = null;
            if (definition.getOptions() != null && definition.getOptions().getLimit() != null) {
                limit = definition.getOptions().getLimit();
            }
            
            // If limit is specified in inputs, use that instead
            if (inputs.containsKey("limit")) {
                limit = Integer.parseInt(inputs.get("limit").toString());
            }
            
            // Ensure query has LIMIT clause if limit is specified
            if (limit != null && !query.toUpperCase().contains("LIMIT")) {
                query += " LIMIT " + limit;
            }
            
            // Execute query
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, inputs);
            
            // Convert to ResultSet
            ResultSet resultSet = new ResultSet();
            List<String> metadata = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();
            
            // If output fields are defined, use them
            if (definition.getOutput() != null && !definition.getOutput().isEmpty()) {
                definition.getOutput().forEach(output -> {
                    String fieldName = output.getField();
                    String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                    metadata.add(alias);
                });
                
                for (Map<String, Object> row : rows) {
                    Map<String, Object> resultRow = new HashMap<>();
                    definition.getOutput().forEach(output -> {
                        String fieldName = output.getField();
                        String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                        resultRow.put(alias, row.get(fieldName));
                    });
                    resultRows.add(resultRow);
                }
            } else {
                // If no output fields are defined, include all fields
                if (!rows.isEmpty()) {
                    metadata.addAll(rows.get(0).keySet());
                }
                resultRows.addAll(rows);
            }
            
            resultSet.setMetadata(metadata);
            resultSet.setRows(resultRows);
            
            return resultSet;
        } catch (Exception e) {
            log.error("Error executing database query", e);
            throw new CallCenterException("Error executing database query", e);
        }
    }
}