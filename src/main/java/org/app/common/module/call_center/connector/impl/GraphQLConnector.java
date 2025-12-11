package org.app.common.module.call_center.connector.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connection.ConnectionManager;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLConnector implements Connector {
    private final ConnectionManager connectionManager;

    @Override
    public ResultSet execute(CallDefinition definition, Map<String, Object> inputs) {
        try {
            // Get GraphQL client from connection manager (with versioning support)
            RestTemplate restTemplate = connectionManager.getGraphQLClient(definition);

            // Build URL
            String url = definition.getRepository().getUrl();

            // Process GraphQL query
            String query = definition.getCall().getQuery();

            // Replace placeholders in query
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                String placeholder = ":" + entry.getKey();
                query = query.replace(placeholder, entry.getValue().toString());
            }

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);

            // Add variables if present
//            if (definition.getCall().getVariables() != null) {
//                Map<String, Object> variables = new HashMap<>();
//                for (Map.Entry<String, Object> entry : definition.getCall().getVariables().entrySet()) {
//                    String key = entry.getKey();
//                    Object value = entry.getValue();
//
//                    if (value instanceof String && ((String) value).startsWith(":")) {
//                        String inputKey = ((String) value).substring(1);
//                        if (inputs.containsKey(inputKey)) {
//                            variables.put(key, inputs.get(inputKey));
//                        }
//                    } else {
//                        variables.put(key, value);
//                    }
//                }
//                requestBody.put("variables", variables);
//            }

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Execute request
            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

            // Convert response to ResultSet
            ResultSet resultSet = new ResultSet();
            List<String> metadata = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                // Get the first entry in the data map (the query result)
                Map.Entry<String, Object> firstEntry = data.entrySet().iterator().next();
                Object queryResult = firstEntry.getValue();

                if (queryResult instanceof List) {
                    // List of objects
                    List<Map<String, Object>> resultList = (List<Map<String, Object>>) queryResult;

                    // If output fields are defined, use them
                    if (definition.getOutput() != null && !definition.getOutput().isEmpty()) {
                        definition.getOutput().forEach(output -> {
                            String fieldName = output.getField();
                            String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                            metadata.add(alias);
                        });

                        for (Map<String, Object> item : resultList) {
                            Map<String, Object> resultRow = new HashMap<>();
                            definition.getOutput().forEach(output -> {
                                String fieldName = output.getField();
                                String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                                resultRow.put(alias, item.get(fieldName));
                            });
                            resultRows.add(resultRow);
                        }
                    } else {
                        // If no output fields are defined, include all fields
                        if (!resultList.isEmpty()) {
                            metadata.addAll(resultList.get(0).keySet());
                        }
                        resultRows.addAll(resultList);
                    }
                } else if (queryResult instanceof Map) {
                    // Single object
                    Map<String, Object> resultObject = (Map<String, Object>) queryResult;

                    // If output fields are defined, use them
                    if (definition.getOutput() != null && !definition.getOutput().isEmpty()) {
                        definition.getOutput().forEach(output -> {
                            String fieldName = output.getField();
                            String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                            metadata.add(alias);
                        });

                        Map<String, Object> resultRow = new HashMap<>();
                        definition.getOutput().forEach(output -> {
                            String fieldName = output.getField();
                            String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                            resultRow.put(alias, resultObject.get(fieldName));
                        });
                        resultRows.add(resultRow);
                    } else {
                        // If no output fields are defined, include all fields
                        metadata.addAll(resultObject.keySet());
                        resultRows.add(resultObject);
                    }
                }
            }

            resultSet.setMetadata(metadata);
            resultSet.setRows(resultRows);

            return resultSet;
        } catch (Exception e) {
            log.error("Error executing GraphQL query", e);
            throw new CallCenterException("Error executing GraphQL query", e);
        }
    }
}
