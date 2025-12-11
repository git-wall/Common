package org.app.common.client.rest.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * Init for all RestTemplate beans to add the LogRequestInterceptor for monitoring HTTP requests.
 */
@Component
@RequiredArgsConstructor
public class RestTemplateBeanPostProcessor implements BeanPostProcessor {

    private final LogRequestInterceptor logRequestInterceptor;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RestTemplate) {
            RestTemplate restTemplate = (RestTemplate) bean;

            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
            if (interceptors.contains(logRequestInterceptor)) {
                return bean; // If the interceptor is already present, do not add it again
            }
            interceptors.add(logRequestInterceptor);
            restTemplate.setInterceptors(interceptors);
        }
        return bean;
    }
}
