package org.app.common.design.legacy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A generic Stage interface that implements a fluent pipeline pattern.
 * This interface provides methods to chain stages, apply transformations,
 * and conditionally execute operations.
 *
 * @param <I> the input type of the stage
 * @param <O> the output type of the stage
 */
public interface Stage<I, O> extends Function<I, O> {

    /**
     * Processes the input through this stage.
     *
     * @param input the input to process
     * @return the processed output
     */
    O process(I input);

    /**
     * Applies this stage to the given input.
     * Default implementation delegates to process method.
     *
     * @param input the input to process
     * @return the processed output
     */
    @Override
    default O apply(I input) {
        return process(input);
    }

    /**
     * Chains this stage with another stage, creating a pipeline.
     *
     * @param <R> the output type of the next stage
     * @param next the next stage in the pipeline
     * @return a new stage that represents the combined pipeline
     */
    default <R> Stage<I, R> then(Stage<O, R> next) {
        return input -> next.process(this.process(input));
    }

    /**
     * Chains this stage with a function, creating a pipeline.
     *
     * @param <R> the output type of the function
     * @param next the function to apply after this stage
     * @return a new stage that represents the combined pipeline
     */
    default <R> Stage<I, R> then(Function<O, R> next) {
        return input -> next.apply(this.process(input));
    }

    /**
     * Executes a consumer on the output of this stage without changing the output.
     *
     * @param action the consumer to execute
     * @return a new stage that executes the consumer and returns the original output
     */
    default Stage<I, O> peek(Consumer<O> action) {
        return input -> {
            O output = this.process(input);
            action.accept(output);
            return output;
        };
    }

    /**
     * Conditionally executes a stage based on a predicate.
     *
     * @param condition the predicate to test
     * @param thenStage the stage to execute if the condition is true
     * @return a new stage that conditionally executes the given stage
     */
    default Stage<I, O> when(Predicate<O> condition, Function<O, O> thenStage) {
        return input -> {
            O output = this.process(input);
            return condition.test(output) ? thenStage.apply(output) : output;
        };
    }

    /**
     * Conditionally executes one of two stages based on a predicate.
     *
     * @param condition the predicate to test
     * @param thenStage the stage to execute if the condition is true
     * @param elseStage the stage to execute if the condition is false
     * @return a new stage that conditionally executes one of the given stages
     */
    default Stage<I, O> when(Predicate<O> condition, Function<O, O> thenStage, Function<O, O> elseStage) {
        return input -> {
            O output = this.process(input);
            return condition.test(output) ? thenStage.apply(output) : elseStage.apply(output);
        };
    }

    /**
     * Handles errors that occur during processing.
     *
     * @param errorHandler the function to handle errors
     * @return a new stage that handles errors
     */
    default Stage<I, O> handleError(Function<Exception, O> errorHandler) {
        return input -> {
            try {
                return this.process(input);
            } catch (Exception e) {
                return errorHandler.apply(e);
            }
        };
    }

    /**
     * Creates a stage from a function.
     *
     * @param <I> the input type
     * @param <O> the output type
     * @param function the function to convert
     * @return a stage that applies the function
     */
    static <I, O> Stage<I, O> of(Function<I, O> function) {
        return function::apply;
    }

    /**
     * Creates an identity stage that returns the input unchanged.
     *
     * @param <T> the type of input and output
     * @return an identity stage
     */
    static <T> Stage<T, T> identity() {
        return input -> input;
    }

    /**
     * Creates a stage that always returns a constant value.
     *
     * @param <I> the input type
     * @param <O> the output type
     * @param value the constant value to return
     * @return a stage that always returns the constant value
     */
    static <I, O> Stage<I, O> constant(O value) {
        return input -> value;
    }

    /**
     * Creates a stage that applies a transformation only if a condition is met.
     *
     * @param <T> the type of input and output
     * @param condition the condition to check
     * @param transformation the transformation to apply if condition is met
     * @return a conditional stage
     */
    static <T> Stage<T, T> conditional(Predicate<T> condition, Function<T, T> transformation) {
        return input -> condition.test(input) ? transformation.apply(input) : input;
    }
}
