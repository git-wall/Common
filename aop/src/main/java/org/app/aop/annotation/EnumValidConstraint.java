package org.app.aop.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidConstraint implements ConstraintValidator<EnumValid, String> {

    private Set<String> values;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        values = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return values.contains(value);
    }
}
