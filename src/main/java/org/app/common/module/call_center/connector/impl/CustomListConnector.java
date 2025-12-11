package org.app.common.module.call_center.connector.impl;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomListConnector implements Connector {

    @Override
    public ResultSet execute(CallDefinition definition, Map<String, Object> inputs) {
        try {
            // Get data from definition
            List<Map<String, Object>> data = definition.getCall().getData();
            
            // Filter data based on inputs
            if (!inputs.isEmpty() && data != null) {
                data = data.stream()
                    .filter(item -> matchesFilters(item, inputs))
                    .collect(Collectors.toList());
            }
            
            // Convert to ResultSet
            ResultSet resultSet = new ResultSet();
            List<String> metadata = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();
            
            if (data != null && !data.isEmpty()) {
                // If output fields are defined, use them
                if (definition.getOutput() != null && !definition.getOutput().isEmpty()) {
                    definition.getOutput().forEach(output -> {
                        String fieldName = output.getField();
                        String alias = output.getAlias() != null ? output.getAlias() : fieldName;
                        metadata.add(alias);
                    });
                    
                    for (Map<String, Object> item : data) {
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
                    metadata.addAll(data.get(0).keySet());
                    resultRows.addAll(data);
                }
            }
            
            resultSet.setMetadata(metadata);
            resultSet.setRows(resultRows);
            
            return resultSet;
        } catch (Exception e) {
            log.error("Error processing custom list data", e);
            throw new CallCenterException("Error processing custom list data", e);
        }
    }
    
    private boolean matchesFilters(Map<String, Object> item, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            Object value = filter.getValue();
            
            if (item.containsKey(key)) {
                Object itemValue = item.get(key);
                
                // Skip if filter value is null
                if (value == null) {
                    continue;
                }
                
                // Check if values match
                if (!value.toString().equals(itemValue.toString())) {
                    return false;
                }
            }
        }
        
        return true;
    }
}