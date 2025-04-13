package org.app.common.machine;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A generic and flexible State Machine implementation in Java.
 * Supports multiple state types, events, and transition actions.
 *
 * @param <S> The type representing states
 * @param <E> The type representing events
 * @param <C> The type representing context data passed during transitions
 */
public class StateMachine<S, E, C> {

    /**
     * Represents a transition between states
     */
    @Getter
    public static class Transition<S, C> {
        private final S source;
        private final S target;
        private final TransitionAction<S, C> action;
        private final Predicate<C> guard;

        public Transition(S source, S target, TransitionAction<S, C> action, Predicate<C> guard) {
            this.source = source;
            this.target = target;
            this.action = action;
            this.guard = guard;
        }

    }

    /**
     * Functional interface for transition actions
     */
    @FunctionalInterface
    public interface TransitionAction<S, C> {
        void execute(S from, S to, C context);
    }

    /**
     * Listener interface for state machine events
     */
    public interface StateMachineListener<S, E, C> {
        default void onTransitionStart(S from, E event, C context) {}
        default void onTransitionComplete(S from, S to, E event, C context) {}
        default void onTransitionDeclined(S state, E event, C context) {}
        default void onError(S state, E event, C context, Exception exception) {}
    }

    /**
     * -- GETTER --
     *  Gets the current state of the machine
     *
     */
    // The current state of the machine
    @Getter
    private S currentState;

    // Map of transitions by event and source state
    private final Map<E, Map<S, Transition<S, C>>> transitions;

    // Map of global transitions by event that can be triggered from any state
    private final Map<E, Transition<S, C>> globalTransitions;

    // Map of exit actions for states
    private final Map<S, TransitionAction<S, C>> exitActions;

    // Map of entry actions for states
    private final Map<S, TransitionAction<S, C>> entryActions;

    // Listeners for state machine events
    private final Map<String, StateMachineListener<S, E, C>> listeners;

    /**
     * Creates a new state machine with the specified initial state
     *
     * @param initialState The initial state of the machine
     */
    public StateMachine(S initialState) {
        this.currentState = initialState;
        this.transitions = new HashMap<>();
        this.globalTransitions = new HashMap<>();
        this.exitActions = new HashMap<>();
        this.entryActions = new HashMap<>();
        this.listeners = new HashMap<>();
    }

    /**
     * Adds a transition to the state machine
     *
     * @param event The event that triggers the transition
     * @param sourceState The source state of the transition
     * @param targetState The target state of the transition
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addTransition(E event, S sourceState, S targetState) {
        return addTransition(event, sourceState, targetState, null, c -> true);
    }

    /**
     * Adds a transition with an action to the state machine
     *
     * @param event The event that triggers the transition
     * @param sourceState The source state of the transition
     * @param targetState The target state of the transition
     * @param action The action to execute during the transition
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addTransition(E event, S sourceState, S targetState, TransitionAction<S, C> action) {
        return addTransition(event, sourceState, targetState, action, c -> true);
    }

    /**
     * Adds a transition with an action and guard to the state machine
     *
     * @param event The event that triggers the transition
     * @param sourceState The source state of the transition
     * @param targetState The target state of the transition
     * @param action The action to execute during the transition
     * @param guard The guard condition that must be satisfied for the transition to occur
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addTransition(E event, S sourceState, S targetState,
                                               TransitionAction<S, C> action, Predicate<C> guard) {
        Transition<S, C> transition = new Transition<>(sourceState, targetState, action, guard);

        transitions.computeIfAbsent(event, e -> new HashMap<>())
                .put(sourceState, transition);

        return this;
    }

    /**
     * Adds a global transition that can be triggered from any state
     *
     * @param event The event that triggers the transition
     * @param targetState The target state of the transition
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addGlobalTransition(E event, S targetState) {
        return addGlobalTransition(event, targetState, null, c -> true);
    }

    /**
     * Adds a global transition with an action that can be triggered from any state
     *
     * @param event The event that triggers the transition
     * @param targetState The target state of the transition
     * @param action The action to execute during the transition
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addGlobalTransition(E event, S targetState, TransitionAction<S, C> action) {
        return addGlobalTransition(event, targetState, action, c -> true);
    }

    /**
     * Adds a global transition with an action and guard that can be triggered from any state
     *
     * @param event The event that triggers the transition
     * @param targetState The target state of the transition
     * @param action The action to execute during the transition
     * @param guard The guard condition that must be satisfied for the transition to occur
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addGlobalTransition(E event, S targetState,
                                                     TransitionAction<S, C> action, Predicate<C> guard) {
        Transition<S, C> transition = new Transition<>(null, targetState, action, guard);
        globalTransitions.put(event, transition);
        return this;
    }

    /**
     * Adds an entry action for a state
     *
     * @param state The state to add the entry action for
     * @param action The action to execute when entering the state
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addEntryAction(S state, TransitionAction<S, C> action) {
        entryActions.put(state, action);
        return this;
    }

    /**
     * Adds an exit action for a state
     *
     * @param state The state to add the exit action for
     * @param action The action to execute when exiting the state
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addExitAction(S state, TransitionAction<S, C> action) {
        exitActions.put(state, action);
        return this;
    }

    /**
     * Adds a listener for state machine events
     *
     * @param id A unique identifier for the listener
     * @param listener The listener to add
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> addListener(String id, StateMachineListener<S, E, C> listener) {
        listeners.put(id, listener);
        return this;
    }

    /**
     * Removes a listener by its identifier
     *
     * @param id The identifier of the listener to remove
     * @return The StateMachine instance for chaining
     */
    public StateMachine<S, E, C> removeListener(String id) {
        listeners.remove(id);
        return this;
    }

    /**
     * Fires an event to trigger a state transition
     *
     * @param event The event to fire
     * @param context The context data for the transition
     * @return True if the transition was successful, false otherwise
     */
    public boolean fireEvent(E event, C context) {
        try {
            // Notify listeners about transition start
            listeners.values().forEach(l -> l.onTransitionStart(currentState, event, context));

            // Check if there's a specific transition for the current state and event
            Optional<Transition<S, C>> transition = Optional.ofNullable(transitions.get(event))
                    .map(t -> t.get(currentState));

            // If no specific transition, check for a global transition
            if (transition.isEmpty()) {
                transition = Optional.ofNullable(globalTransitions.get(event));
            }

            // If no transition is found or guard condition fails, decline the transition
            if (transition.isEmpty() || !transition.get().getGuard().test(context)) {
                listeners.values().forEach(l -> l.onTransitionDeclined(currentState, event, context));
                return false;
            }

            // Get the target state
            S targetState = transition.get().getTarget();
            S sourceState = currentState;

            // Execute exit action if it exists
            if (exitActions.containsKey(currentState)) {
                exitActions.get(currentState).execute(currentState, targetState, context);
            }

            // Execute the transition action if it exists
            if (transition.get().getAction() != null) {
                transition.get().getAction().execute(currentState, targetState, context);
            }

            // Update the current state
            currentState = targetState;

            // Execute entry action if it exists
            if (entryActions.containsKey(currentState)) {
                entryActions.get(currentState).execute(sourceState, currentState, context);
            }

            // Notify listeners about transition completion
            listeners.values().forEach(l -> l.onTransitionComplete(sourceState, currentState, event, context));

            return true;
        } catch (Exception e) {
            // Notify listeners about error
            listeners.values().forEach(l -> l.onError(currentState, event, context, e));
            return false;
        }
    }

    /**
     * Resets the state machine to the specified state
     *
     * @param state The state to reset to
     */
    public void reset(S state) {
        this.currentState = state;
    }
}
