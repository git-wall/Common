package org.app.common.module.scoring_rules.formula;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\d+(\\.\\d+)?|SUM|SUB|MUL|DIV\\d+|[A-Z_]+|\\(|\\)|,"
    );

    public static Node parse(String expression) {
        Stack<List<Node>> exprStack = new Stack<>();
        Stack<String> opStack = new Stack<>();
        exprStack.push(new ArrayList<>());

        Matcher matcher = TOKEN_PATTERN.matcher(expression);
        while (matcher.find()) {
            String token = matcher.group();

            if (token.matches("\\d+(\\.\\d+)?")) {
                exprStack.peek().add(new NumberNode(new BigDecimal(token)));
            } else if (token.matches("SUM|SUB|MUL|DIV")) {
                opStack.push(token);
                exprStack.push(new ArrayList<>());
            } else if (token.equals(")")) {
                List<Node> operands = exprStack.pop();
                String op = opStack.pop();
                exprStack.peek().add(new OperatorNode(op, operands));
            }
        }
        return exprStack.pop().get(0);
    }

    public static String replaceTemplateWithObj(String template, Object object) {
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                template = template.replace(field.getName().toUpperCase(), String.valueOf(field.get(object)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return template;
    }

    public static String replaceFormulaWithRule(String formula, String value) {
        return formula.replaceAll("\\bRULE_VALUE\\b", value); // replace RULE_VALUE only
    }

    // if rule not have reference but in formula have it mean you need to extract it
    public static Set<Long> extractRuleDependencies(String formula) {
        if (!StringUtils.hasText(formula)) return Collections.emptySet();

        Set<Long> dependencies = new HashSet<>(4, 1f);
        Matcher matcher = Pattern.compile("RULE_(\\d+)").matcher(formula);
        while (matcher.find()) {
            dependencies.add(Long.valueOf(matcher.group(1)));
        }
        return dependencies;
    }

    public static String replaceRuleResults(String formula, Map<Long, BigDecimal> ruleResults) {
        if (formula == null) return null;

        Matcher matcher = Pattern.compile("RULE_(\\d+)").matcher(formula);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String idStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Long id = Long.parseLong(idStr);
            BigDecimal value = ruleResults.getOrDefault(id, BigDecimal.ZERO);
            matcher.appendReplacement(sb, value.toPlainString());
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
