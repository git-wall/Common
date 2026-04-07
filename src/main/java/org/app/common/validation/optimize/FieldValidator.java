package org.app.common.validation.optimize;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Field validator interface
 */
@FunctionalInterface
public interface FieldValidator {
    String validate(JsonNode tree);
}
