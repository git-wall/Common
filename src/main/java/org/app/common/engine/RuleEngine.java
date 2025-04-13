package org.app.common.engine;

import lombok.extern.slf4j.Slf4j;
import org.app.common.algorithms.dag.Dag;
import org.app.common.algorithms.dag.HashDag;
import org.app.common.engine.formula.ExpressionParser;
import org.app.common.engine.formula.Node;
import org.app.common.engine.rule.Rule;
import org.app.common.engine.rule.RuleResult;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.app.common.engine.formula.ExpressionParser.*;

// uppercase for combine all
@Slf4j
public class RuleEngine {

    /**
     * Evaluates a list of rules against a given request object and returns the result of the first rule
     * that satisfies its condition.
     *
     * @param rules   the list of rules to evaluate
     * @param request the object containing the data required for rule evaluation
     * @return the result of the first rule that satisfies its condition, or null if no rule matches
     */
    public static RuleResult evaluate(List<Rule> rules, Object request) {
        return rules.stream()
                .filter(rule -> rule.evaluateCondition(request))
                .findFirst()
                .map(rule -> evaluateRule(rules, rule, request))
                .orElse(null);
    }

    /**
     * Evaluates a specific rule and its dependencies against a given object.
     *
     * <p>
     * The method identifies all rules that the specified rule depends on, evaluates them, and then
     * evaluates the specified rule itself.
     * </p>
     *
     * @param allRule the list of all available rules
     * @param rule    the rule to evaluate
     * @param object  the object containing the data required for rule evaluation
     * @return the result of the evaluated rule, or null if the rule cannot be evaluated
     */
    public static RuleResult evaluateRule(List<Rule> allRule, Rule rule, Object object) {
        List<Rule> rules = allRule.stream()
                .filter(r -> rule.getReferences().contains(r.getId()))
                .collect(Collectors.toList());
        rules.add(rule);

        return evaluateRules(rules, object)
                .stream()
                .filter(r -> Objects.equals(rule.getId(), r.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @param rules  List of rules to be evaluated
     * @param object The object containing fields required for rule evaluation
     * @return List of RuleResult containing the results of the evaluated rules
     */
    public static List<RuleResult> evaluateRules(List<Rule> rules, Object object) {
        Map<Long, BigDecimal> resultMap = new HashMap<>();
        Dag<Rule> dag = buildDAG(rules);

        return dag.topologicalSort()
                .stream()
                .map(rule -> {
                    String formula = replaceValInStr(object, rule, resultMap);
                    Node ast = parse(formula);
                    BigDecimal result = ast.evaluate();
                    resultMap.put(rule.getId(), result);
                    return new RuleResult(rule, result);
                })
                .collect(Collectors.toList());
    }

    private static @NotNull String replaceValInStr(Object object, Rule rule, Map<Long, BigDecimal> resultMap) {
        String formula = replaceTemplateWithObj(rule.getFormula(), object);
        formula = replaceRuleResults(formula, resultMap);
        formula = replaceFormulaWithRule(formula, rule.getValue().toPlainString());
        return formula;
    }

    /**
     * Builds a Directed Acyclic Graph (DAG) from the list of rules.
     *
     * @param rules List of rules to be added to the DAG
     * @return A DAG representing the dependencies between rules
     */
    private static Dag<Rule> buildDAG(List<Rule> rules) {
        Map<Long, Rule> ruleMap = rules.stream().collect(Collectors.toMap(Rule::getId, Function.identity()));
        Dag<Rule> dag = new HashDag<>();

        for (Rule rule : rules) {
            dag.add(rule);

            Set<Long> refs = getRefs(rule);

            for (Long refId : refs) {
                Rule referred = ruleMap.get(refId);
                if (referred != null) {
                    // we need to calculate referred before calculate main rule
                    // because main have no result of the referred
                    // Ex: DAG(rule1 -> rule2)
                    dag.putIfAcyclic(referred, rule);
                }
            }
        }

        return dag;
    }

    /**
     * Retrieves the references (dependencies) of a rule.
     *
     * @param rule The rule for which to retrieve references
     * @return A set of rule IDs that the given rule depends on
     */
    private static Set<Long> getRefs(Rule rule) {
        Set<Long> refs = rule.getReferences();
        if (refs == null) {
            refs = ExpressionParser.extractRuleDependencies(rule.getFormula());
            rule.setReferences(refs);
        }
        return refs;
    }
}
