package org.app.common.module.scoring_rules.condition;

import lombok.Getter;
import lombok.Setter;
import org.app.common.module.scoring_rules.contain.Operator;
import org.app.common.module.scoring_rules.contain.ValueType;

import java.util.List;

@Setter
@Getter
public class Condition{
    private long id;
    private String name;
    private Operator operator;
    private List<Object> values;
    private ValueType valueType;
}
