package org.app.common.module.call_center.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.connector.Connector;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.InputField;
import org.app.common.module.call_center.model.ResultSet;
import org.app.common.module.call_center.repository.CallDefinitionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {
    private final Map<String, Connector> connectors;

    @Qualifier("databaseCallDefinitionRepository")
    private final CallDefinitionRepository databaseRepository;

    @Qualifier("jsonFileCallDefinitionRepository")
    private final CallDefinitionRepository jsonFileRepository;

    @Qualifier("propertiesCallDefinitionRepository")
    private final CallDefinitionRepository propertiesRepository;

    // Default repository to use
    private CallDefinitionRepository activeRepository;

    @PostConstruct
    public void init() {
        // Choose the repository with the most definitions
        List<CallDefinition> dbDefs = databaseRepository.findAll();
        List<CallDefinition> jsonDefs = jsonFileRepository.findAll();
        List<CallDefinition> propDefs = propertiesRepository.findAll();

        if (dbDefs.size() >= jsonDefs.size() && dbDefs.size() >= propDefs.size()) {
            activeRepository = databaseRepository;
            log.info("Using database repository with {} definitions", dbDefs.size());
        } else if (jsonDefs.size() >= dbDefs.size() && jsonDefs.size() >= propDefs.size()) {
            activeRepository = jsonFileRepository;
            log.info("Using JSON file repository with {} definitions", jsonDefs.size());
        } else {
            activeRepository = propertiesRepository;
            log.info("Using properties repository with {} definitions", propDefs.size());
        }
    }

    /**
     * Execute a call definition with the given inputs
     * @param code The call definition code
     * @param inputs The input parameters
     * @return The result set
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResultSet execute(String code, Map<String, Object> inputs) {
        // Find call definition
        Optional<CallDefinition> definitionOpt = activeRepository.findByCode(code);
        if (definitionOpt.isEmpty()) {
            // Try other repositories if not found
            definitionOpt = databaseRepository.findByCode(code);
            if (definitionOpt.isEmpty()) {
                definitionOpt = jsonFileRepository.findByCode(code);
                if (definitionOpt.isEmpty()) {
                    definitionOpt = propertiesRepository.findByCode(code);
                    if (definitionOpt.isEmpty()) {
                        throw new CallCenterException("Call definition not found: " + code);
                    }
                }
            }
        }

        CallDefinition definition = definitionOpt.get();

        // Validate inputs
        validateInputs(definition, inputs);

        // Apply default values for missing inputs
        Map<String, Object> processedInputs = applyDefaultValues(definition, inputs);

        // Get connector
        String connectorType = definition.getRepository().getType();
        Connector connector = connectors.get(connectorType + "Connector");

        if (connector == null) {
            throw new CallCenterException("Connector not found for type: " + connectorType);
        }

        try {
            // Execute call
            ResultSet result = connector.execute(definition, processedInputs);

            // Apply limit if specified in options
            if (definition.getOptions() != null && definition.getOptions().getLimit() != null) {
                int limit = definition.getOptions().getLimit();
                if (result.getRows().size() > limit) {
                    result.setRows(result.getRows().subList(0, limit));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error executing call: {}", code, e);

            // Try fallback if defined
            if (definition.getOptions() != null && definition.getOptions().getFallbackDefinition() != null) {
                String fallbackCode = definition.getOptions().getFallbackDefinition();
                log.info("Using fallback definition: {}", fallbackCode);
                return execute(fallbackCode, inputs);
            }

            throw new CallCenterException("Error executing call: " + code, e);
        }
    }

    /**
     * Refresh all definitions from all repositories
     */
    public void refreshAllDefinitions() {
        log.info("Refreshing all call definitions");
        databaseRepository.refreshDefinitions();
        jsonFileRepository.refreshDefinitions();
        propertiesRepository.refreshDefinitions();
    }

    /**
     * Validate inputs against the call definition
     * @param definition The call definition
     * @param inputs The input parameters
     */
    private void validateInputs(CallDefinition definition, Map<String, Object> inputs) {
        if (definition.getInput() == null) {
            return;
        }

        for (InputField inputField : definition.getInput()) {
            String name = inputField.getName();
            boolean required = inputField.getRequired();

            if (required && !inputs.containsKey(name) && inputField.getDefaultValue() == null) {
                throw new CallCenterException("Missing required input: " + name);
            }

            if (inputs.containsKey(name)) {
                Object value = inputs.get(name);
                String type = inputField.getType();

                // Validate type
                if (type != null && value != null) {
                    switch (type.toLowerCase()) {
                        case "int":
                        case "integer":
                            try {
                                Integer.parseInt(value.toString());
                            } catch (NumberFormatException e) {
                                throw new CallCenterException("Invalid integer value for input: " + name);
                            }
                            break;
                        case "double":
                        case "float":
                            try {
                                Double.parseDouble(value.toString());
                            } catch (NumberFormatException e) {
                                throw new CallCenterException("Invalid decimal value for input: " + name);
                            }
                            break;
                        case "boolean":
                            if (!(value instanceof Boolean) &&
                                    !value.toString().equalsIgnoreCase("true") &&
                                    !value.toString().equalsIgnoreCase("false")) {
                                throw new CallCenterException("Invalid boolean value for input: " + name);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Apply default values for missing inputs
     * @param definition The call definition
     * @param inputs The input parameters
     * @return The processed inputs
     */
    private Map<String, Object> applyDefaultValues(CallDefinition definition, Map<String, Object> inputs) {
        Map<String, Object> processedInputs = new HashMap<>(inputs);

        if (definition.getInput() == null) {
            return processedInputs;
        }

        for (InputField inputField : definition.getInput()) {
            String name = inputField.getName();

            if (!processedInputs.containsKey(name) && inputField.getDefaultValue() != null) {
                processedInputs.put(name, inputField.getDefaultValue());
            }
        }

        return processedInputs;
    }
}
