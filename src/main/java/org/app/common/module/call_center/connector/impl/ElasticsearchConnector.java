package org.app.common.module.call_center.connector.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connection.ConnectionManager;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchConnector implements Connector {
    private final ConnectionManager connectionManager;
    private final ObjectMapper objectMapper;

    @Override
    public ResultSet execute(CallDefinition definition, Map<String, Object> inputs) {
        try {
            // Get Elasticsearch client from connection manager (with versioning support)
            RestTemplate restTemplate = (RestTemplate) connectionManager.getElasticsearchClient(definition);

            // Build URL
            String url = definition.getRepository().getUrl() + "/" + definition.getRepository().getIndex() + "/_search";

            // Process DSL query
            Map<String, Object> dsl = new HashMap<>((Integer) definition.getCall().getDsl());
            processDsl(dsl, inputs);

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(dsl, headers);

            // Execute request
            Map<String, Object> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            ).getBody();

            // Convert response to ResultSet
            ResultSet resultSet = new ResultSet();
            List<String> metadata = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();

            if (response != null && response.containsKey("hits")) {
                Map<String, Object> hits = (Map<String, Object>) response.get("hits");
                List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");

                for (Map<String, Object> hit : hitsList) {
                    Map<String, Object> source = (Map<String, Object>) hit.get("_source");

                    // If output fields are defined, use them
                    if (definition.getOutput() != null && !definition.getOutput().isEmpty()) {
                        if (metadata.isEmpty()) {
                            definition.getOutput().forEach(output -> {
                                String fieldName = output.getField();
                                String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                                metadata.add(alias);
                            });
                        }

                        Map<String, Object> resultRow = new HashMap<>();
                        definition.getOutput().forEach(output -> {
                            String fieldName = output.getField();
                            String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                            resultRow.put(alias, source.get(fieldName));
                        });
                        resultRows.add(resultRow);
                    } else {
                        // If no output fields are defined, include all fields
                        if (metadata.isEmpty()) {
                            metadata.addAll(source.keySet());
                        }
                        resultRows.add(source);
                    }
                }
            }

            resultSet.setMetadata(metadata);
            resultSet.setRows(resultRows);

            return resultSet;
        } catch (Exception e) {
            log.error("Error executing Elasticsearch query", e);
            throw new CallCenterException("Error executing Elasticsearch query", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void processDsl(Map<String, Object> dsl, Map<String, Object> inputs) {
        // Process each entry in the DSL
        for (Map.Entry<String, Object> entry : new HashMap<>(dsl).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.startsWith(":")) {
                    String inputKey = strValue.substring(1);
                    if (inputs.containsKey(inputKey)) {
                        dsl.put(key, inputs.get(inputKey));
                    }
                }
            } else if (value instanceof Map) {
                processDsl((Map<String, Object>) value, inputs);
            } else if (value instanceof List) {
                processList((List<Object>) value, inputs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processList(List<Object> list, Map<String, Object> inputs) {
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.startsWith(":")) {
                    String inputKey = strValue.substring(1);
                    if (inputs.containsKey(inputKey)) {
                        list.set(i, inputs.get(inputKey));
                    }
                }
            } else if (value instanceof Map) {
                processDsl((Map<String, Object>) value, inputs);
            } else if (value instanceof List) {
                processList((List<Object>) value, inputs);
            }
        }
    }
}
