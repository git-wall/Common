package org.app.common.engine.rule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.ThreadContext;
import org.app.common.engine.condition.Condition;
import org.app.common.engine.condition.ConditionGroup;
import org.app.common.engine.contain.Operator;
import org.app.common.engine.contain.RuleType;
import org.app.common.engine.contain.ValueType;
import org.app.common.utils.ObjUtils;
import org.thymeleaf.util.ListUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Slf4j
public class Rule {
    private long id;
    private String name;
    private RuleType type;
    private BigDecimal value;

    // Example: "SUM(SUM(MUL(PRICE, QUANTITY), RULE_VALUE), RULE_24)"
    // RULE_VALUE = Rule.value
    // RULE_24 = result from formula of Rule id 24
    private String formula;

    // IDs of other rules referenced in this rule's formula
    private Set<Long> references;

    // list of condition groups is OR, each group is AND
    private List<ConditionGroup> conditionGroups; // Multiple groups of conditions

    public BigDecimal getValue() {
        if (type == RuleType.PERCENT) {
            value = value.divide(BigDecimal.valueOf(100L), MathContext.DECIMAL128);
        }
        return value;
    }

    public boolean evaluateCondition(Object request) {
        if (ListUtils.isEmpty(conditionGroups)) {
            log.error("{} - conditions is null or empty", ThreadContext.getRequestId());
            return false;
        }

        return conditionGroups.stream()
                .anyMatch(conditionGroup -> conditionGroup.getConditions()
                        .stream()
                        .allMatch(condition -> isMatch(request, condition)));
    }

    private boolean isMatch(Object request, Condition cond) {
        Object actualValue = ObjUtils.getValFromField(request, cond.getName());
        Object normalizedActual = ValueType.normalize(cond.getValueType(), actualValue);
        List<Object> normalizedValues = cond.getValues().stream()
                .map(o -> ValueType.normalize(cond.getValueType(), o))
                .collect(Collectors.toList());

        return Operator.getFun(cond.getOperator()).apply(normalizedValues, normalizedActual);
    }
}
