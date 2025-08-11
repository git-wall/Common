package org.app.common.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.app.common.agent.dispatch.TraceDispatcher;
import org.app.common.agent.transform.TraceTransformer;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class DynamicAgent {
    private static final List<TraceDispatcher> dispatchers = new ArrayList<>();

    public static void premain(String arguments, Instrumentation instrumentation) {
        configureAgent(instrumentation);
    }

    public static void agentmain(String arguments, Instrumentation instrumentation) {
        configureAgent(instrumentation);
    }

    public static void registerDispatcher(TraceDispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    private static void configureAgent(Instrumentation instrumentation) {
        CompositeTraceDispatcher compositeDispatcher = new CompositeTraceDispatcher(dispatchers);

        new AgentBuilder.Default()
                .type(ElementMatchers.any())
                .transform((builder,
                            typeDescription,
                            classLoader,
                            javaModule,
                            protectionDomain) ->
                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(new TraceTransformer(compositeDispatcher))))
                .installOn(instrumentation);
    }

    private static class CompositeTraceDispatcher implements TraceDispatcher {
        private final List<TraceDispatcher> dispatchers;

        public CompositeTraceDispatcher(List<TraceDispatcher> dispatchers) {
            this.dispatchers = new ArrayList<>(dispatchers);
        }

        @Override
        public void dispatch(String traceId, java.util.Map<String, Object> context) {
            for (TraceDispatcher dispatcher : dispatchers) {
                try {
                    dispatcher.dispatch(traceId, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
