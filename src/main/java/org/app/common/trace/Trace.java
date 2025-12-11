package org.app.common.trace;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Trace {
    private final Tracer tracer;

    public Span getSpan() {
        return Optional.ofNullable(tracer.currentSpan()).orElse(tracer.nextSpan());
    }

    public TraceContext getContext() {
        return Optional.ofNullable(tracer.currentSpan()).orElse(tracer.nextSpan()).context();
    }

    public String getId() {
        return Optional.ofNullable(tracer.currentSpan()).orElse(tracer.nextSpan()).context().traceIdString();
    }
}
