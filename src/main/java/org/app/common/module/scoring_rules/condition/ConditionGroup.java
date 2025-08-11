package org.app.common.module.scoring_rules.condition;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ConditionGroup {
    private long id;
    private long ruleId; // Reference to the parent rule
    private int groupOrder; // For ordering groups
    private List<Condition> conditions; // Conditions within a group are AND together
}
