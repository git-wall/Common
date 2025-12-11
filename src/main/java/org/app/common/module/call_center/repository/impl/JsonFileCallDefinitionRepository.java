package org.app.common.module.call_center.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.repository.CallDefinitionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("jsonFileCallDefinitionRepository")
public class JsonFileCallDefinitionRepository implements CallDefinitionRepository {
    private final ObjectMapper objectMapper;
    
    @Value("${call-center.definitions.file:classpath:call-definitions.json}")
    private String definitionsFile;
    
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
        log.info("Refreshing call definitions from JSON file: {}", definitionsFile);
        definitionCache.clear();
        
        try {
            File file = new File(definitionsFile.replace("classpath:", ""));
            if (!file.exists()) {
                log.warn("Definitions file not found: {}", definitionsFile);
                return;
            }
            
            List<CallDefinition> definitions = objectMapper.readValue(file, new TypeReference<List<CallDefinition>>() {});
            definitions.forEach(def -> definitionCache.put(def.getCode(), def));
            log.info("Loaded {} call definitions from JSON file", definitions.size());
        } catch (IOException e) {
            log.error("Error loading call definitions from JSON file", e);
            throw new CallCenterException("Error loading call definitions from JSON file", e);
        }
    }
}