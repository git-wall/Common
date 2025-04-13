```java
import lombok.Getter;

public class StateMachineExamples {

    /**
     * A simple traffic light example with enum states and events
     */
    public static class TrafficLightExample {

        // Define the possible states
        public enum LightState {
            RED, YELLOW, GREEN
        }

        // Define the possible events
        public enum LightEvent {
            TIMER, EMERGENCY
        }

        // Context class to pass data during transitions
        public static class LightContext {
            @Getter
            private int timeInState;
            private boolean isEmergency;

            public LightContext(int timeInState, boolean isEmergency) {
                this.timeInState = timeInState;
                this.isEmergency = isEmergency;
            }

            public boolean isEmergency() {
                return isEmergency;
            }
        }

        public static void main(String[] args) {
            // Create the state machine with initial state RED
            StateMachine<LightState, LightEvent, LightContext> trafficLight =
                    new StateMachine<>(LightState.RED);

            // Add normal transitions
            trafficLight.addTransition(LightEvent.TIMER, LightState.RED, LightState.GREEN,
                            (from, to, ctx) -> System.out.println("Changing from RED to GREEN"))

                    .addTransition(LightEvent.TIMER, LightState.GREEN, LightState.YELLOW,
                            (from, to, ctx) -> System.out.println("Changing from GREEN to YELLOW"))

                    .addTransition(LightEvent.TIMER, LightState.YELLOW, LightState.RED,
                            (from, to, ctx) -> System.out.println("Changing from YELLOW to RED"));

            // Add global transition for emergency
            trafficLight.addGlobalTransition(LightEvent.EMERGENCY, LightState.RED,
                    (from, to, ctx) -> System.out.println("EMERGENCY! Changing to RED from " + from),
                    ctx -> ctx.isEmergency());

            // Add state entry and exit actions
            trafficLight.addEntryAction(LightState.RED,
                            (from, to, ctx) -> System.out.println("Entered RED state"))

                    .addExitAction(LightState.RED,
                            (from, to, ctx) -> System.out.println("Exited RED state"));

            // Add a listener
            trafficLight.addListener("debugListener", new StateMachine.StateMachineListener<LightState, LightEvent, LightContext>() {
                @Override
                public void onTransitionComplete(LightState from, LightState to, LightEvent event, LightContext context) {
                    System.out.println("Transition completed: " + from + " -> " + to + " on event " + event);
                }
            });

            // Fire some events
            trafficLight.fireEvent(LightEvent.TIMER, new LightContext(30, false));
            System.out.println("Current state: " + trafficLight.getCurrentState());

            trafficLight.fireEvent(LightEvent.TIMER, new LightContext(20, false));
            System.out.println("Current state: " + trafficLight.getCurrentState());

            trafficLight.fireEvent(LightEvent.EMERGENCY, new LightContext(0, true));
            System.out.println("Current state: " + trafficLight.getCurrentState());
        }
    }

    /**
     * A document processing example with String states and events
     */
    public static class DocumentProcessingExample {

        // Context class for document processing
        public static class DocumentContext {
            // Getters
            @Getter
            private String documentId;
            @Getter
            private String documentContent;
            @Getter
            private String userId;
            private boolean isApproved;

            public DocumentContext(String documentId, String documentContent, String userId, boolean isApproved) {
                this.documentId = documentId;
                this.documentContent = documentContent;
                this.userId = userId;
                this.isApproved = isApproved;
            }

            public boolean isApproved() { return isApproved; }

            // Setters
            public void setApproved(boolean approved) { isApproved = approved; }
        }

        public static void main(String[] args) {
            // Create the state machine with initial state "DRAFT"
            StateMachine<String, String, DocumentContext> documentProcessor =
                    new StateMachine<>("DRAFT");

            // Add transitions
            documentProcessor.addTransition("SUBMIT", "DRAFT", "REVIEW",
                            (from, to, ctx) -> System.out.println("Document " + ctx.getDocumentId() + " submitted for review"))

                    .addTransition("APPROVE", "REVIEW", "APPROVED",
                            (from, to, ctx) -> {
                                ctx.setApproved(true);
                                System.out.println("Document " + ctx.getDocumentId() + " approved");
                            },
                            ctx -> ctx.getUserId().startsWith("MANAGER_"))

                    .addTransition("REJECT", "REVIEW", "DRAFT",
                            (from, to, ctx) -> System.out.println("Document " + ctx.getDocumentId() + " rejected"))

                    .addTransition("PUBLISH", "APPROVED", "PUBLISHED",
                            (from, to, ctx) -> System.out.println("Document " + ctx.getDocumentId() + " published"),
                            ctx -> ctx.isApproved());

            // Add a global transition for cancellation
            documentProcessor.addGlobalTransition("CANCEL", "CANCELLED",
                    (from, to, ctx) -> System.out.println("Document " + ctx.getDocumentId() + " cancelled from " + from));

            // Process a document
            DocumentContext doc = new DocumentContext("DOC-123", "Some content", "USER_1", false);

            documentProcessor.fireEvent("SUBMIT", doc);
            System.out.println("Current state: " + documentProcessor.getCurrentState());

            // This will fail because the user is not a manager
            boolean result = documentProcessor.fireEvent("APPROVE", doc);
            System.out.println("Approval result: " + result);
            System.out.println("Current state: " + documentProcessor.getCurrentState());

            // Update the context and try again with a manager user
            doc = new DocumentContext("DOC-123", "Some content", "MANAGER_1", false);
            documentProcessor.fireEvent("APPROVE", doc);
            System.out.println("Current state: " + documentProcessor.getCurrentState());

            documentProcessor.fireEvent("PUBLISH", doc);
            System.out.println("Current state: " + documentProcessor.getCurrentState());

            // Cancel from any state
            documentProcessor.fireEvent("CANCEL", doc);
            System.out.println("Current state: " + documentProcessor.getCurrentState());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Traffic Light Example ===");
        TrafficLightExample.main(args);

        System.out.println("\n=== Document Processing Example ===");
        DocumentProcessingExample.main(args);
    }
}
```