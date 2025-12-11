package org.app.common.module.call_center.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.repository.CallDefinitionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("propertiesCallDefinitionRepository")
public class PropertiesCallDefinitionRepository implements CallDefinitionRepository {
    private final Environment environment;
    private final ObjectMapper objectMapper;
    
    // Cache for call definitions
    private final Map<String, CallDefinition> definitionCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        refreshDefinitions();
    }
    
    @Override
    public Optional<CallDefinition> findByCode(String code) {
        return Optional.ofNullable(definitionCache.get(code));
    }
    
    @Override
    public List<CallDefinition> findAll() {
        return new ArrayList<>(definitionCache.values());
    }
    
    @Override
    public void refreshDefinitions() {
        log.info("Refreshing call definitions from properties");
        definitionCache.clear();
        
        try {
            String countStr = environment.getProperty("call-center.definitions.count", "0");
            int count = Integer.parseInt(countStr);
            
            for (int i = 0; i < count; i++) {
                String definitionJson = environment.getProperty("call-center.definitions." + i);
                if (definitionJson != null) {
                    CallDefinition definition = objectMapper.readValue(definitionJson, CallDefinition.class);
                    definitionCache.put(definition.getCode(), definition);
                }
            }
            
            log.info("Loaded {} call definitions from properties", definitionCache.size());
        } catch (Exception e) {
            log.error("Error loading call definitions from properties", e);
            throw new CallCenterException("Error loading call definitions from properties", e);
        }
    }
}