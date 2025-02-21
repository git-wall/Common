package org.app.common.rule;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
/**
 * <pre>
 * Pattern: Rule-Based Decision Engine
 * A Rule-Based Decision Engine allows you to define and execute business rules dynamically.
 * This pattern is useful for scenarios where decisions need to be made based on a set of conditions,
 * such as validating user input, determining eligibility for a service,
 * or applying discounts based on customer behavior.
 * </pre>
 * <blockquote><pre>
 *  public class AgeRule implements Rule<Integer> {
 *     private final int minAge;
 *
 *     public AgeRule(int minAge) {
 *         this.minAge = minAge;
 *     }
 *
 *     public boolean evaluate(Integer age) {
 *         return age >= minAge;
 *     }
 *
 *     public String getDescription() {
 *         return "Age must be at least " + minAge;
 *     }
 * }
 *
 * public class IncomeRule implements Rule<Double> {
 *     private final double minIncome;
 *
 *     public IncomeRule(double minIncome) {
 *         this.minIncome = minIncome;
 *     }
 *
 *     public boolean evaluate(Double income) {
 *         return income >= minIncome;
 *     }
 *
 *     public String getDescription() {
 *         return "Income must be at least " + minIncome;
 *     }
 * }
 * // main
 * public static void main(String[] args) {
 *         DecisionEngine<Integer> ageEngine = new DecisionEngine<>();
 *         ageEngine.addRule(new AgeRule(18));
 *         ageEngine.addRule(new AgeRule(21));
 *
 *         DecisionEngine<Double> incomeEngine = new DecisionEngine<>();
 *         incomeEngine.addRule(new IncomeRule(30000.0));
 *         incomeEngine.addRule(new IncomeRule(50000.0));
 *
 *         // Evaluate age rules
 *         DecisionEngine.DecisionResult ageResult = ageEngine.evaluate(20);
 *         System.out.println("Passed Age Rules: " + ageResult.getPassedRules());
 *         System.out.println("Failed Age Rules: " + ageResult.getFailedRules());
 *
 *         // Evaluate income rules
 *         DecisionEngine.DecisionResult incomeResult = incomeEngine.evaluate(40000.0);
 *         System.out.println("Passed Income Rules: " + incomeResult.getPassedRules());
 *         System.out.println("Failed Income Rules: " + incomeResult.getFailedRules());
 *     }
 * </blockquote></pre>
 *  <pre>
 * Benefits
 *      Flexibility : Rules can be added, removed, or modified without changing the core logic.
 *      Reusability : Individual rules can be reused across different decision engines.
 *      Extensibility : New rules can be added easily.
 *      Transparency : Clear separation of concerns makes it easier to trace issues.
 *  </pre>
 * */
public class DecisionEngine<T> {

    private final List<Rule<T>> rules = new ArrayList<>();

    public void addRule(Rule<T> rule) {
        rules.add(rule);
    }

    public DecisionResult evaluate(T input) {
        List<String> passedRules = new ArrayList<>();
        List<String> failedRules = new ArrayList<>();

        for (Rule<T> rule : rules) {
            if (rule.evaluate(input)) {
                passedRules.add(rule.getDescription());
            } else {
                failedRules.add(rule.getDescription());
            }
        }

        return new DecisionResult(passedRules, failedRules);
    }

    @Getter
    public static class DecisionResult {
        private final List<String> passedRules;
        private final List<String> failedRules;

        public DecisionResult(List<String> passedRules, List<String> failedRules) {
            this.passedRules = passedRules;
            this.failedRules = failedRules;
        }

        public boolean isAllRulesPassed() {
            return failedRules.isEmpty();
        }
    }
}
