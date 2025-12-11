package org.app.common.module.call_center.repository;

import org.app.common.module.call_center.model.CallDefinition;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CallDefinition
 */
public interface CallDefinitionRepository {
    /**
     * Find a call definition by its code
     * @param code The call definition code
     * @return The call definition if found
     */
    Optional<CallDefinition> findByCode(String code);
    
    /**
     * Find all call definitions
     * @return List of all call definitions
     */
    List<CallDefinition> findAll();
    
    /**
     * Refresh all definitions from the source
     * This is used to reload definitions when they change
     */
    void refreshDefinitions();
}