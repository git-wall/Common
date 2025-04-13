package org.app.common.cglib;

import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkflowProxyFactory implements MethodInterceptor {

    private final Object target;
    private final Map<String, List<WorkflowStep>> workflowMap;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public WorkflowProxyFactory(Object target, Map<String, List<WorkflowStep>> workflowMap) {
        this.target = target;
        this.workflowMap = workflowMap;
    }

    public Object createProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Workflow wf = method.getAnnotation(Workflow.class);
        if (wf != null) {
            String flowName = wf.value();
            int currentOrder = wf.order();

            Object result = proxy.invoke(target, args); // execute current step

            executor.submit(() -> {
                List<WorkflowStep> steps = workflowMap.get(flowName);
                if (steps != null) {
                    for (WorkflowStep step : steps) {
                        if (step.order > currentOrder) {
                            try {
                                step.method.invoke(step.target); // future: pass args if needed
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            return result;
        }

        return proxy.invoke(target, args);
    }
}