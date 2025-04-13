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
 *     public boolean condition(Integer age) {
 *         return age >= minAge;
 *     }
 *
 *     @Override
 *     public String getDescription() {
 *         return "Age must be at least " + minAge;
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
    boolean condition(T input);
    String getDescription();
}
