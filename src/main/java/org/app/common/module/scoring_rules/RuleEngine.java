package org.app.common.module.scoring_rules;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.scoring_rules.formula.ExpressionParser;
import org.app.common.module.scoring_rules.formula.Node;
import org.app.common.module.scoring_rules.rule.Rule;
import org.app.common.module.scoring_rules.rule.RuleResult;
import org.app.common.pipeline.v2.Pipeline;
import org.app.common.struct.dag.Dag;
import org.app.common.struct.dag.HashDag;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.app.common.module.scoring_rules.formula.ExpressionParser.*;

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
     * @param rules  the list of all available rules
     * @param rule   the rule to evaluate
     * @param object the object containing the data required for rule evaluation
     * @return the result of the evaluated rule, or null if the rule cannot be evaluated
     */
    public static RuleResult evaluateRule(List<Rule> rules, Rule rule, Object object) {
        return Pipeline.from(rules)
                .filter(r -> rule.getReferences().contains(r.getId()))
                .then(e -> e.add(rule))
                .sinks(e -> evaluateRules(e, object))
                .filter(r -> Objects.equals(rule.getId(), r.getId()))
                .findFirstOrElse(null);
    }

    /**
     * @param rules  List of rules to be evaluated
     * @param object The object containing fields required for rule evaluation
     * @return List of RuleResult containing the results of the evaluated rules
     */
    public static List<RuleResult> evaluateRules(Collection<Rule> rules, Object object) {
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

    /**
     * Replaces placeholders in a formula string with actual values derived from the provided object,
     * rule, and result map.
     *
     * <p>
     * This method performs the following steps:
     * 1. Replaces template placeholders in the rule's formula with values from the provided object.
     * 2. Replaces placeholders for rule results with values from the result map.
     * 3. Replaces placeholders for formulas with the rule's value as a string.
     * </p>
     *
     * @param object    The object containing fields required for template replacement.
     * @param rule      The rule whose formula and value are used for replacement.
     * @param resultMap A map containing previously computed rule results, where the key is the rule ID
     *                  and the value is the result as a BigDecimal.
     * @return A string representing the formula with all placeholders replaced by actual values.
     */
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
    public static Dag<Rule> buildDAG(Collection<Rule> rules) {
        Map<Long, Rule> ruleMap = rules.stream().collect(Collectors.toMap(Rule::getId, Function.identity()));
        Dag<Rule> dag = new HashDag<>();

        for (Rule rule : rules) {
            dag.add(rule);

            Set<Long> refs = getRefs(rule);

            for (Long refId : refs) {
                Rule referred = ruleMap.get(refId);
                if (referred != null) {
                    // we need to calculate referred before calculate the main rule
                    // because main has no result of the referred
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
