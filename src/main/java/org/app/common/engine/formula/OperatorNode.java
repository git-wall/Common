package org.app.common.engine.formula;

import org.app.common.engine.contain.Operator;

import java.math.BigDecimal;
import java.util.List;

public class OperatorNode extends Node {
    private final String operator;
    private final List<Node> operands;

    public OperatorNode(String operator, List<Node> operands) {
        this.operator = operator;
        this.operands = operands;
    }

    @Override
    public BigDecimal evaluate() {
        switch (Operator.valueOf(operator)) {
            case SUM:
                return operands.stream()
                        .map(Node::evaluate)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            case SUB:
                return operands.stream()
                        .map(Node::evaluate)
                        .reduce(BigDecimal::subtract)
                        .orElse(BigDecimal.ZERO);
            case MUL:
                return operands.stream()
                        .map(Node::evaluate)
                        .reduce(BigDecimal.ONE, BigDecimal::multiply);
            case DIV:
                return operands.stream()
                        .map(Node::evaluate)
                        .reduce(BigDecimal::divide)
                        .orElse(BigDecimal.ZERO);
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    @Override
    public String toString() {
        return operator + "(" + operands + ")";
    }
}
