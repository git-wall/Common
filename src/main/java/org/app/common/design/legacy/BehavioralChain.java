package org.app.common.design.legacy;

import java.util.ArrayList;
import java.util.List;

// Pattern: Behavioral Chain of Responsibility
/**
 * <blockquote><pre>{@code
 * public static void main(String[] args) {
 *         BehavioralChain<String> chain = new BehavioralChain<>();
 *
 *         // Add behaviors to the chain
 *         chain.addBehavior(new ValidationBehavior());
 *         chain.addBehavior(new LoggingBehavior());
 *         chain.addBehavior(new TransformationBehavior());
 *
 *         // Execute the chain with input
 *         String input = "hello world";
 *         try {
 *             String result = chain.execute(input);
 *             System.out.println("Final Result: " + result);
 *         } catch (IllegalArgumentException e) {
 *             System.err.println("Error: " + e.getMessage());
 *         }
 *     }
 * }</blockquote></pre>
 *
 * <pre>
 * Benefits
 *      Decoupling : The core application does not need to know about the specifics of each behavior.
 *      Reusability : Behaviors can be reused across different parts of the application.
 *      Scalability : Supports an unlimited number of behaviors.
 *      Testability : Individual behaviors can be tested independently.
 * </pre>
 * */
public class BehavioralChain<T> {

    private final List<Behavior<T>> behaviors = new ArrayList<>(16);

    public void add(Behavior<T> behavior) {
        behaviors.add(behavior);
    }

    public void remove(Behavior<T> behavior) {
        behaviors.remove(behavior);
    }

    public T execute(T input) {
        for (Behavior<T> behavior : behaviors) {
            input = behavior.execute(input);
        }
        return input;
    }
}