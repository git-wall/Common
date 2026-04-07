package org.app.observation.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContextKey {
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
}
