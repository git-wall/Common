package org.app.common.rule;
/**
 * Pattern: Rule-Based Decision Engine
 * A Rule-Based Decision Engine allows you to define and execute business rules dynamically.
 * This pattern is useful for scenarios where decisions need to be made based on a set of conditions,
 * such as validating user input, determining eligibility for a service,
 * or applying discounts based on customer behavior.
 *
 * <pre>
 * {@code
 *  public class AgeRule implements Rule<Integer> {
 *     private final int minAge;
 *
 *     public AgeRule(int minAge) {
 *         this.minAge = minAge;
 *     }
 *
 *     @Override
 *     public boolean evaluate(Integer age) {
 *         return age >= minAge;
 *     }
 *
 *     @Override
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
 *     @Override
 *     public boolean evaluate(Double income) {
 *         return income >= minIncome;
 *     }
 *
 *     @Override
 *     public String getDescription() {
 *         return "Income must be at least " + minIncome;
 *     }
 * }
 *
 * //
 *
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
 * }
 * </>
 *
 * <pre>
 * Benefits
 *      Flexibility : Rules can be added, removed, or modified without changing the core logic.
 *      Reusability : Individual rules can be reused across different decision engines.
 *      Extensibility : New rules can be added easily.
 *      Transparency : Clear separation of concerns makes it easier to trace issues.
 * </>
 */
public interface Rule<T> {
    boolean evaluate(T input);
    String getDescription();
}
