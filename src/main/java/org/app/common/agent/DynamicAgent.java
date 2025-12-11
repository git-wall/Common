package org.app.common.agent;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.app.common.agent.dispatch.TraceDispatcher;
import org.app.common.agent.transform.TraceTransformer;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class DynamicAgent {

    private DynamicAgent () {
        // Prevent instantiation
    }

    private static final List<TraceDispatcher> dispatchers = new ArrayList<>();

    public static void premain(Instrumentation instrumentation) {
        configureAgent(instrumentation);
    }

    public static void agentmain(Instrumentation instrumentation) {
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

    @RequiredArgsConstructor
    private static class CompositeTraceDispatcher implements TraceDispatcher {
        private final List<TraceDispatcher> dispatchers;

        @Override
        @SneakyThrows
        public void dispatch(String traceId, java.util.Map<String, Object> context) {
            for (TraceDispatcher dispatcher : dispatchers) {
                dispatcher.dispatch(traceId, context);
            }
        }
    }
}
