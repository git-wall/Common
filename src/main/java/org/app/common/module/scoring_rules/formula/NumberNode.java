package org.app.common.module.scoring_rules.formula;

import java.math.BigDecimal;

public class NumberNode extends Node {
    private final BigDecimal value;

    public NumberNode(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal evaluate() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
