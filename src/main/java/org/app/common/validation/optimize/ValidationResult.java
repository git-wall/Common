package org.app.common.validation.optimize;

import lombok.Data;

@Data
public class ValidationResult<T> {
    private boolean valid;
    private String error;
    private T data;

    public static <T> ValidationResult<T> success(T data) {
        ValidationResult<T> result = new ValidationResult<>();
        result.valid = true;
        result.data = data;
        return result;
    }

    public static <T> ValidationResult<T> error(String error) {
        ValidationResult<T> result = new ValidationResult<>();
        result.valid = false;
        result.error = error;
        return result;
    }
}
