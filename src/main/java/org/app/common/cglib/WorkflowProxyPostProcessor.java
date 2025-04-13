package org.app.common.cglib;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class WorkflowProxyPostProcessor implements BeanPostProcessor {

    private final WorkflowRegistry registry;

    public WorkflowProxyPostProcessor(WorkflowRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Workflow.class)) {
                return new WorkflowProxyFactory(bean, registry.getWorkflowMap()).createProxy();
            }
        }
        return bean;
    }
}
