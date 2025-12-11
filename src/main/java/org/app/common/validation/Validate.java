package org.app.common.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Validator;

@Service
@RequiredArgsConstructor
public class Validate {
    private final Validator validator;

    public <T> void valid(T dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new javax.validation.ConstraintViolationException(violations);
        }
    }
}
