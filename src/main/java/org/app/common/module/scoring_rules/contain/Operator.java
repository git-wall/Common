package org.app.common.module.scoring_rules.contain;

import org.app.common.utils.ObjUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum Operator {
    // arithmetic operators
    SUM, SUB, MUL, DIV,
    // logical operators
    EQUAL, NOT_EQUAL, GREATER_THAN, LESS_THAN, BETWEEN, IN, NOT_IN;

    private static final Map<Operator, BiFunction<List<Object>, Object, Boolean>> OPERATOR_FUNCTIONS;

    static {
        OPERATOR_FUNCTIONS = Map.of(
                EQUAL, (values, actualValue) -> values.stream().anyMatch(value -> value.equals(actualValue)),
                NOT_EQUAL, (values, actualValue) -> values.stream().noneMatch(value -> value.equals(actualValue)),
                GREATER_THAN, (values, actualValue) -> values.stream().anyMatch(value -> ObjUtils.compare(actualValue, value) > 0),
                LESS_THAN, (values, actualValue) -> values.stream().anyMatch(value -> ObjUtils.compare(actualValue, value) < 0),
                BETWEEN, (values, actualValue) -> {
                    if (values.size() != 2) return false;
                    return ObjUtils.compare(actualValue, values.get(0)) > 0 && ObjUtils.compare(actualValue, values.get(1)) < 0;
                },
                IN, (values, actualValue) -> values.stream().anyMatch(value -> value.equals(actualValue)),
                NOT_IN, (values, actualValue) -> values.stream().noneMatch(value -> value.equals(actualValue))
        );
    }

    public static BiFunction<List<Object>, Object, Boolean> getFun(Operator operator) {
        BiFunction<List<Object>, Object, Boolean> function = OPERATOR_FUNCTIONS.get(operator);
        if (function == null) {
            throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
        return function;
    }
}
