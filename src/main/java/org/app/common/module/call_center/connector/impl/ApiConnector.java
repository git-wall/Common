package org.app.common.module.call_center.connector.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connection.ConnectionManager;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.AuthInfo;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiConnector implements Connector {
    private final ConnectionManager connectionManager;
    private final ObjectMapper objectMapper;

    @Override
    public ResultSet execute(CallDefinition definition, Map<String, Object> inputs) {
        try {
            // Get REST client from connection manager (with versioning support)
            RestTemplate restTemplate = connectionManager.getApiClient(definition);

            // Build URL
            String baseUrl = definition.getRepository().getBaseUrl();
            String endpoint = definition.getCall().getEndpoint();
            String url = baseUrl + endpoint;

            // Build URI with path variables
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

            // Add query parameters if present
            if (definition.getCall().getParams() != null) {
                for (Map.Entry<String, Object> entry : definition.getCall().getParams().entrySet()) {
                    String value = entry.getValue().toString();
                    // If value is a placeholder, replace with input value
                    if (value.startsWith(":")) {
                        String inputKey = value.substring(1);
                        if (inputs.containsKey(inputKey)) {
                            builder.queryParam(entry.getKey(), inputs.get(inputKey));
                        }
                    } else {
                        builder.queryParam(entry.getKey(), value);
                    }
                }
            }

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Handle authentication
            if (definition.getRepository().getAuth() != null) {
                handleAuthentication(definition, headers);
            }

            // Build request entity
            HttpMethod method = HttpMethod.valueOf(definition.getCall().getMethod());
            HttpEntity<?> requestEntity;

            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                // Build request body from inputs
                Map<String, Object> requestBody = new HashMap<>();
                if (definition.getCall().getBody() != null) {
//                    for (Map.Entry<String, Object> entry : definition.getCall().getBody().entrySet()) {
//                        String value = entry.getValue().toString();
//                        // If a value is a placeholder, replace it with input value
//                        if (value.startsWith(":")) {
//                            String inputKey = value.substring(1);
//                            if (inputs.containsKey(inputKey)) {
//                                requestBody.put(entry.getKey(), inputs.get(inputKey));
//                            }
//                        } else {
//                            requestBody.put(entry.getKey(), value);
//                        }
//                    }
                }
                requestEntity = new HttpEntity<>(requestBody, headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            // Execute request
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                builder.build().toUri(),
                method,
                requestEntity,
                Map.class
            );

            // Convert response to ResultSet
            ResultSet resultSet = new ResultSet();
            List<String> metadata = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();

            if (responseEntity.getBody() != null) {
                // Extract data from response
                Map<String, Object> responseBody = responseEntity.getBody();

                // If response is an array, convert to list of maps
                if (responseBody.containsKey("data") && responseBody.get("data") instanceof List) {
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) responseBody.get("data");

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
                } else {
                    // Single object response
                    Map<String, Object> row = responseBody;

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
                            resultRow.put(alias, row.get(fieldName));
                        });
                        resultRows.add(resultRow);
                    } else {
                        // If no output fields are defined, include all fields
                        metadata.addAll(row.keySet());
                        resultRows.add(row);
                    }
                }
            }

            resultSet.setMetadata(metadata);
            resultSet.setRows(resultRows);

            return resultSet;
        } catch (Exception e) {
            log.error("Error executing API call", e);
            throw new CallCenterException("Error executing API call", e);
        }
    }

    private void handleAuthentication(CallDefinition definition, HttpHeaders headers) {
        AuthInfo auth = definition.getRepository().getAuth();

        // Check if token is expired
        if (auth.getCurrentToken() != null && auth.getExpiresAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(auth.getExpiresAt())) {
                // Token is expired, refresh it
                refreshToken(definition);
            }
        } else {
            // No token, get a new one
            refreshToken(definition);
        }

        // Add token to headers
        if (auth.getCurrentToken() != null) {
            String tokenHeader = auth.getHeader();
            String tokenPrefix = auth.getPrefix() != null ? auth.getPrefix() : "";
            headers.set(tokenHeader, tokenPrefix + auth.getCurrentToken());
        }
    }

    private void refreshToken(CallDefinition definition) {
        try {
            AuthInfo auth = definition.getRepository().getAuth();

            // Get REST client
            RestTemplate restTemplate = new RestTemplate();

            // Build URL
            String baseUrl = definition.getRepository().getBaseUrl();
            String tokenEndpoint = auth.getTokenEndpoint();
            String url = baseUrl + tokenEndpoint;

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request entity
            HttpMethod method = HttpMethod.valueOf(auth.getMethod());
            HttpEntity<?> requestEntity = new HttpEntity<>(auth.getBody(), headers);

            // Execute request
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url,
                method,
                requestEntity,
                Map.class
            );

            // Extract token from response
            if (responseEntity.getBody() != null) {
                String tokenField = auth.getTokenField();
                String token = responseEntity.getBody().get(tokenField).toString();

                // Update auth info
                auth.setCurrentToken(token);

                // Set expiration time (default: 1 hour)
                auth.setExpiresAt(LocalDateTime.now().plusHours(1));
            }
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new CallCenterException("Error refreshing token", e);
        }
    }
}
