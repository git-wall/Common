package org.app.common.module.call_center.connector;

import org.app.common.module.call_center.model.CallDefinition;
import org.app.common.module.call_center.model.ResultSet;

import java.util.Map;

/**
 * Interface for all data source connectors
 */
public interface Connector {
    /**
     * Execute a call to the data source
     * @param def The call definition
     * @param inputs The input parameters
     * @return Standardized result set
     */
    ResultSet execute(CallDefinition def, Map<String, Object> inputs);
}