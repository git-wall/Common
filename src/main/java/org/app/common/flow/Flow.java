package org.app.common.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Pattern: Dynamic Workflow Engine
 * <blockquote><pre>
 *    {@code
 * // prepare steps
 * public class ValidateInputStep implements WorkflowStep<String> {
 *     @Override
 *     public String execute(String input) throws Exception {
 *         if (input == null || input.isEmpty()) {
 *             throw new IllegalArgumentException("Input cannot be null or empty");
 *         }
 *         System.out.println("Validation passed for input: " + input);
 *         return input;
 *     }
 * }
 *
 * public class TransformInputStep implements WorkflowStep<String> {
 *     @Override
 *     public String execute(String input) {
 *         String transformed = input.toUpperCase();
 *         System.out.println("Transformed input: " + transformed);
 *         return transformed;
 *     }
 * }
 *
 * public class NotifyUserStep implements WorkflowStep<String> {
 *     @Override
 *     public String execute(String input) {
 *         System.out.println("Notifying user with message: " + input);
 *         return input;
 *     }
 * }
 *
 * // Usage:
 * Flow<String> flow = new Flow<>()
 *                     .addStep(new ValidateInputStep())
 *                     .addStep(new TransformInputStep())
 *                     .addStep(new NotifyUserStep());
 *
 *  // Execute the workflow
 *  String result = workflow.execute("hello world");
 *    }
 * </blockquote></pre>
 *
 * <pre>
 * Benefits
 *      Flexibility : Workflows can be defined and modified at runtime.
 *      Reusability : Individual steps can be reused across different workflows.
 *      Extensibility : New steps can be added without modifying existing code.
 *      Error Handling : Each step can handle errors independently, making debugging easier.
 * </pre>
 */
public class Flow<T> {
    private List<Step<T>> steps;

    public Flow<T> begin(int size) {
        steps = new ArrayList<Step<T>>(size);
        return this;
    }

    public Flow<T> next(Step<T> step) {
        steps.add(step);
        return this;
    }

    public void doing(T input) {
        for (Step<T> step : steps) {
            step.execute(input);
        }
    }

    public T execute(T input) {
        for (Step<T> step : steps) {
            input = step.execute(input);
        }
        return input;
    }

    public <R> R sink(T input, Function<T, R> function) {
        for (Step<T> step : steps) {
            input = step.execute(input);
        }
        return function.apply(input);
    }

    public interface Step<T> {
        T execute(T input);
    }
}
