package org.app.common.cglib;

import java.lang.reflect.Method;

public class WorkflowStep {
    public final Object target;
    public final Method method;
    public final int order;

    public WorkflowStep(Object target, Method method, int order) {
        this.target = target;
        this.method = method;
        this.order = order;
    }
}