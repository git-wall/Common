package org.app.common.module.scoring_rules.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.app.common.module.scoring_rules.contain.RuleType;

import java.math.BigDecimal;

// request -> rule -> result
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
