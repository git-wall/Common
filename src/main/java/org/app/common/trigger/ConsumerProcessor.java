package org.app.common.trigger;

import lombok.SneakyThrows;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class ConsumerProcessor implements BeanPostProcessor {

    private final Map<String, List<ListenerMetadata>> groupedListeners = new HashMap<>(16, 0.75f);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @Nullable String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        MethodIntrospector.MetadataLookup<Consumer> lookup = m -> AnnotationUtils.findAnnotation(m, Consumer.class).orElse(null);
        Map<Method, Consumer> methods = MethodIntrospector.selectMethods(clazz, lookup);

        methods.forEach((method, consumer) -> groupedListeners
                .computeIfAbsent(consumer.group(), k -> new ArrayList<>())
                .add(new ListenerMetadata(method, bean)));

        return bean;
    }

    public void processData(Object data, String group) {
        List<ListenerMetadata> listeners = groupedListeners.getOrDefault(group, Collections.emptyList());

        for (ListenerMetadata metadata : listeners) {
            extracted(data, metadata);
        }
    }

    @SneakyThrows
    private void extracted(Object data, ListenerMetadata metadata) {
        metadata.method.invoke(metadata.bean, data);
    }

    static class ListenerMetadata {
        final Method method;
        final Object bean;

        ListenerMetadata(Method method, Object bean) {
            this.method = method;
            this.bean = bean;
        }
    }
}
