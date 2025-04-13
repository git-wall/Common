package org.app.common.engine.rule;

import lombok.Getter;
import lombok.Setter;
import org.app.common.engine.contain.RuleType;

import java.math.BigDecimal;

// request -> rule -> result
@Setter
@Getter
public class RuleResult {
    private Long id;
    private String ruleName;
    private RuleType ruleType;
    private BigDecimal result;

    public RuleResult(Rule rule, BigDecimal result) {
        this.id = rule.getId();
        this.ruleName = rule.getName();
        this.ruleType = rule.getType();
        this.result = result;
    }
}
