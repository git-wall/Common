package org.app.common.module.call_center.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.call_center.exception.CallCenterException;
import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.repository.CallDefinitionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("databaseCallDefinitionRepository")
public class DatabaseCallDefinitionRepository implements CallDefinitionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache for call definitions
    private final Map<String, CallDefinition> definitionCache = new ConcurrentHashMap<>();
    
    @Override
    public Optional<CallDefinition> findByCode(String code) {
        // Check cache first
        if (definitionCache.containsKey(code)) {
            return Optional.of(definitionCache.get(code));
        }
        
        try {
            String sql = "SELECT * FROM call_definitions WHERE code = ?";
            List<CallDefinition> definitions = jdbcTemplate.query(sql, 
                (rs, rowNum) -> {
                    try {
                        String json = rs.getString("definition_json");
                        CallDefinition def = objectMapper.readValue(json, CallDefinition.class);
                        def.setId(rs.getString("id"));
                        def.setCode(rs.getString("code"));
                        def.setName(rs.getString("name"));
                        def.setDescription(rs.getString("description"));
                        def.setVersion(rs.getInt("version"));
                        return def;
                    } catch (Exception e) {
                        throw new CallCenterException("Error parsing call definition", e);
                    }
                }, 
                code);
            
            if (definitions.isEmpty()) {
                return Optional.empty();
            }
            
            CallDefinition definition = definitions.get(0);
            // Update cache
            definitionCache.put(code, definition);
            return Optional.of(definition);
        } catch (Exception e) {
            log.error("Error finding call definition by code: {}", code, e);
            throw new CallCenterException("Error finding call definition by code: " + code, e);
        }
    }
    
    @Override
    public List<CallDefinition> findAll() {
        try {
            String sql = "SELECT * FROM call_definitions";
            List<CallDefinition> definitions = jdbcTemplate.query(sql, 
                (rs, rowNum) -> {
                    try {
                        String json = rs.getString("definition_json");
                        CallDefinition def = objectMapper.readValue(json, CallDefinition.class);
                        def.setId(rs.getString("id"));
                        def.setCode(rs.getString("code"));
                        def.setName(rs.getString("name"));
                        def.setDescription(rs.getString("description"));
                        def.setVersion(rs.getInt("version"));
                        return def;
                    } catch (Exception e) {
                        throw new CallCenterException("Error parsing call definition", e);
                    }
                });
            
            // Update cache
            definitions.forEach(def -> definitionCache.put(def.getCode(), def));
            return definitions;
        } catch (Exception e) {
            log.error("Error finding all call definitions", e);
            throw new CallCenterException("Error finding all call definitions", e);
        }
    }
    
    @Override
    public void refreshDefinitions() {
        log.info("Refreshing call definitions from database");
        definitionCache.clear();
        findAll(); // This will repopulate the cache
    }
}