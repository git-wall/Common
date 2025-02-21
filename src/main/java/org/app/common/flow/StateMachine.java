package org.app.common.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * Pattern: State Machine for Workflow Management
 * <blockquote><pre>{@code
 * public static void main(String[] args) {
 *         StateMachine stateMachine = new StateMachine();
 *
 *         // Add states to the state machine
 *         stateMachine.addState("idle", new IdleState());
 *         stateMachine.addState("processing", new ProcessingState());
 *         stateMachine.addState("completed", new CompletedState());
 *
 *         // Transition through states
 *         stateMachine.transitionTo("idle");
 *         stateMachine.executeCurrentState();
 *
 *         stateMachine.transitionTo("processing");
 *         stateMachine.executeCurrentState();
 *
 *         stateMachine.transitionTo("completed");
 *         stateMachine.executeCurrentState();
 *     }
 * }</blockquote></pre>
 * <pre>
 * Benefits:
 *      Predictability : Ensures that workflows follow a well-defined sequence of states.
 *      Extensibility : New states and transitions can be added easily.
 *      Debugging : Clear separation of concerns makes it easier to trace issues.
 *      Reusability : States can be reused across different workflows.
 *  </pre>
 * */
public class StateMachine {
    private final Map<String, State> states = new HashMap<>(16, 0.75f);
    private State currentState;

    public void add(String name, State state) {
        states.put(name, state);
    }

    public void currentState(String stateName) {
        State nextState = states.get(stateName);
        if (nextState == null) {
            throw new IllegalArgumentException("State not found: " + stateName);
        }
        if (currentState != null) {
            currentState.exit();
        }
        currentState = nextState;
        currentState.enter();
    }

    public void execute() {
        if (currentState == null) {
            throw new IllegalStateException("No current state set");
        }
        currentState.execute();
    }
}
