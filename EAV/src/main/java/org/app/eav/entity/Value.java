package org.app.eav.entity;

import lombok.Data;

/**
 * Represents the "Value" part of the EAV (Entity-Attribute-Value) model.
 * This class stores the actual values for attributes of specific entity instances.
 */
@Data
public class Value {
    private Long id;
    private Integer attributeId;     // Reference to the attribute this value is for
    private String value;         // The actual value stored as a string (can be converted to the appropriate type based on the attribute's dataType)
}
