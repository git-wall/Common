package org.app.common.validation.optimize;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Pre-built validators
 */
public class FieldValidators {

    public static FieldValidator required(String fieldName) {
        return tree -> {
            JsonNode node = tree.get(fieldName);
            if (node == null || node.isNull()) {
                return "Missing required field: " + fieldName;
            }
            return null;
        };
    }

    public static FieldValidator notEmpty(String fieldName) {
        return tree -> {
            JsonNode node = tree.get(fieldName);
            if (node == null || node.isNull()) {
                return fieldName + " cannot be null";
            }
            if (node.isTextual() && node.asText().trim().isEmpty()) {
                return fieldName + " cannot be empty";
            }
            if (node.isArray() && node.isEmpty()) {
                return fieldName + " cannot be empty array";
            }
            return null;
        };
    }

    public static FieldValidator positive(String fieldName) {
        return tree -> {
            JsonNode node = tree.get(fieldName);
            if (node != null && node.isNumber()) {
                if (node.asLong() <= 0) {
                    return fieldName + " must be positive";
                }
            }
            return null;
        };
    }

    public static FieldValidator email(String fieldName) {
        return tree -> {
            JsonNode node = tree.get(fieldName);
            if (node != null && node.isTextual()) {
                String value = node.asText();
                if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    return fieldName + " must be valid email";
                }
            }
            return null;
        };
    }

    public static FieldValidator minLength(String fieldName, int min) {
        return tree -> {
            JsonNode node = tree.get(fieldName);
            if (node != null && node.isTextual()) {
                if (node.asText().length() < min) {
                    return fieldName + " must be at least " + min + " characters";
                }
            }
            return null;
        };
    }
}
