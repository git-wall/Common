package org.app.eav.entity;

import lombok.Data;
import org.app.eav.contain.DataSourceType;
import org.app.eav.contain.ScopeType;
import org.app.eav.contain.ValueType;

/**
 * Represents the "Attribute" part of the EAV (Entity-Attribute-Value) model.
 * This class defines the attributes (properties) that can be associated with entities.
 */
@Data
public class Attribute {
    private Integer id;
    private String name;
    private String description;
    private ValueType valueType;    // The type of value (e.g., "text", "number", "date", etc.)
    private DataSourceType dataSourceType;     // The data type of the value (e.g., "string", "integer", "double", etc.)
    private ScopeType scope;     // The visibility scope of the attribute
    private Integer entityId;   // Reference to the entity type this attribute belongs to
}
