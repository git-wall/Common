package org.app.common.cglib;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;

@Component
@RequiredArgsConstructor
public class WorkflowRegistry {

    private final ApplicationContext applicationContext;

    @Getter
    private final Map<String, List<WorkflowStep>> workflowMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Workflow.class)) {
                    Workflow wf = method.getAnnotation(Workflow.class);
                    workflowMap
                            .computeIfAbsent(wf.value(), k -> new ArrayList<>())
                            .add(new WorkflowStep(bean, method, wf.order()));
                }
            }
        }

        // Sort each workflow by order
        workflowMap.values().forEach(list ->
                list.sort(Comparator.comparingInt(step -> step.order))
        );
    }
}
