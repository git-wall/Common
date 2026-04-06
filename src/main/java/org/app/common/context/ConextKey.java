package org.app.common.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(AccessLevel.PUBLIC)
public class ConextKey {
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
}
