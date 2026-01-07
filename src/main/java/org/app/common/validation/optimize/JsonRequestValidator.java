package org.app.common.validation.optimize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tối ưu hóa này có tác dụng thực sự với các API có lưu lượng truy cập cao và tỷ lệ không hợp lệ cao.
 * Nhưng chỉ nên áp dụng cho các endpoint "nóng" nhất, không phải toàn bộ ứng dụng!
 * */
@Component
public class JsonRequestValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generic validation method
     */
    public <T> ValidationResult<T> validateAndParse(
        InputStream inputStream,
        Class<T> targetClass,
        FieldValidator... fieldValidators) throws IOException {

        JsonParser parser = objectMapper.getFactory().createParser(inputStream);
        JsonNode tree = objectMapper.readTree(parser);

        // 1. Validate từng field theo rules
        for (FieldValidator validator : fieldValidators) {
            String error = validator.validate(tree);
            if (error != null) {
                return ValidationResult.error(error);
            }
        }

        // 2. Convert to target object
        T object = objectMapper.treeToValue(tree, targetClass);

        return ValidationResult.success(object);
    }
}

