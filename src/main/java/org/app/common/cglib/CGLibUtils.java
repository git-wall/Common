package org.app.common.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.app.common.annotation.Description;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/***
 * AOP
 */
@Description(detail = "This class define target class have action doing with each method | AOP")
public class CGLibUtils {

    /**
     * Represents code to run before and after a method call.
     */
    public static class MethodAdvice {
        private final Runnable before;
        private final Runnable after;

        public MethodAdvice(Runnable before, Runnable after) {
            this.before = before;
            this.after = after;
        }

        public static MethodAdvice before(Runnable before) {
            return new MethodAdvice(before, null);
        }

        public static MethodAdvice after(Runnable after) {
            return new MethodAdvice(null, after);
        }

        public static MethodAdvice around(Runnable before, Runnable after) {
            return new MethodAdvice(before, after);
        }
    }

    /**
     * Method matcher interface for determining if a method should be intercepted.
     */
    public interface MethodMatcher {
        boolean matches(Method method);
    }

    /**
     * Matcher for specific method names.
     */
    public static class MethodNameMatcher implements MethodMatcher {
        private final String methodName;

        public MethodNameMatcher(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean matches(Method method) {
            return method.getName().equals(methodName);
        }
    }

    /**
     * Matcher for methods with specific annotations.
     */
    public static class AnnotationMatcher implements MethodMatcher {
        private final Class<? extends Annotation> annotationClass;
        private final String annotationValue;

        public AnnotationMatcher(Class<? extends Annotation> annotationClass) {
            this(annotationClass, null);
        }

        public AnnotationMatcher(Class<? extends Annotation> annotationClass, String annotationValue) {
            this.annotationClass = annotationClass;
            this.annotationValue = annotationValue;
        }

        @Override
        public boolean matches(Method method) {
            if (!method.isAnnotationPresent(annotationClass)) {
                return false;
            }

            if (annotationValue == null) {
                return true;
            }

            try {
                Annotation annotation = method.getAnnotation(annotationClass);
                Method valueMethod = annotationClass.getDeclaredMethod("value");
                String value = (String) valueMethod.invoke(annotation);
                return annotationValue.equals(value);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Matcher based on custom predicate.
     */
    public static class PredicateMatcher implements MethodMatcher {
        private final Predicate<Method> predicate;

        public PredicateMatcher(Predicate<Method> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean matches(Method method) {
            return predicate.test(method);
        }
    }

    /**
     * Main interceptor that delegates to appropriate matchers and advice.
     */
    public static class FlexibleInterceptor implements MethodInterceptor {
        private final Map<MethodMatcher, MethodAdvice> interceptors = new HashMap<>(16, 0.75f);

        public void addInterceptor(MethodMatcher matcher, MethodAdvice advice) {
            interceptors.put(matcher, advice);
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            // Store current method for annotation-driven interception
            setCurrentMethod(method);

            try {
                MethodAdvice advice = interceptors.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().matches(method))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(null);

                // If we found an interceptor, apply it
                if (advice != null) {
                    if (advice.before != null) {
                        advice.before.run();
                    }

                    Object result = proxy.invokeSuper(obj, args);

                    if (advice.after != null) {
                        advice.after.run();
                    }

                    return result;
                } else {
                    // No interceptor found, just call the original method
                    return proxy.invokeSuper(obj, args);
                }
            } finally {
                // Clear thread local to prevent memory leaks
                currentMethod.remove();
            }
        }
    }

    /**
     * Builder for configuring interception.
     */
    public static class InterceptorBuilder<T> {
        private final Class<T> targetClass;
        private final FlexibleInterceptor interceptor = new FlexibleInterceptor();

        public InterceptorBuilder(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        public InterceptorBuilder<T> interceptMethod(String methodName, MethodAdvice advice) {
            interceptor.addInterceptor(new MethodNameMatcher(methodName), advice);
            return this;
        }

        public InterceptorBuilder<T> interceptAnnotatedMethods(Class<? extends Annotation> annotation, MethodAdvice advice) {
            interceptor.addInterceptor(new AnnotationMatcher(annotation), advice);
            return this;
        }

        public InterceptorBuilder<T> interceptAnnotatedMethods(Class<? extends Annotation> annotation, String value, MethodAdvice advice) {
            interceptor.addInterceptor(new AnnotationMatcher(annotation, value), advice);
            return this;
        }

        public InterceptorBuilder<T> interceptMethodsMatching(Predicate<Method> predicate, MethodAdvice advice) {
            interceptor.addInterceptor(new PredicateMatcher(predicate), advice);
            return this;
        }

        public InterceptorBuilder<T> interceptAllMethods(MethodAdvice advice) {
            interceptor.addInterceptor(method -> true, advice);
            return this;
        }

        public T build() {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(targetClass);
            enhancer.setCallback(interceptor);
            return targetClass.cast(enhancer.create());
        }
    }

    /**
     * Create an interceptor builder for the given class.
     */
    public static <T> InterceptorBuilder<T> intercept(Class<T> targetClass) {
        return new InterceptorBuilder<>(targetClass);
    }

    /**
     * Simple method to intercept a specific method.
     */
    public static <T> T interceptMethod(Class<T> targetClass, String methodName, Runnable before, Runnable after) {
        return intercept(targetClass)
                .interceptMethod(methodName, MethodAdvice.around(before, after))
                .build();
    }

    /**
     * Simple method to intercept all methods.
     */
    public static <T> T interceptAllMethods(Class<T> targetClass, Runnable before, Runnable after) {
        return intercept(targetClass)
                .interceptAllMethods(MethodAdvice.around(before, after))
                .build();
    }

    /**
     * Create a proxy that automatically intercepts methods based on annotations.
     *
     * @param targetClass    The class to proxy
     * @param interceptorMap Map of annotation values to their corresponding interceptors
     * @return The proxy instance
     */
    public static <T> T createAnnotationDrivenProxy(Class<T> targetClass,
                                                    Map<String, MethodAdvice> interceptorMap) {
        InterceptorBuilder<T> builder = intercept(targetClass);

        // Add interceptors for @BeforeInterception
        builder.interceptAnnotatedMethods(BeforeIntercept.class,
                MethodAdvice.before(() -> {
                    Method method = getCurrentMethod();
                    if (method != null) {
                        BeforeIntercept annotation = method.getAnnotation(BeforeIntercept.class);
                        String key = annotation.value();
                        MethodAdvice advice = interceptorMap.get(key);
                        if (advice != null && advice.before != null) {
                            advice.before.run();
                        }
                    }
                }));

        // Add interceptors for @AfterInterception
        builder.interceptAnnotatedMethods(AfterIntercept.class,
                MethodAdvice.after(() -> {
                    Method method = getCurrentMethod();
                    if (method != null) {
                        AfterIntercept annotation = method.getAnnotation(AfterIntercept.class);
                        String key = annotation.value();
                        MethodAdvice advice = interceptorMap.get(key);
                        if (advice != null && advice.after != null) {
                            advice.after.run();
                        }
                    }
                }));

        // Add interceptors for @AroundInterception
        builder.interceptAnnotatedMethods(AroundIntercept.class,
                new MethodAdvice(
                        () -> {
                            Method method = getCurrentMethod();
                            if (method != null) {
                                AroundIntercept annotation = method.getAnnotation(AroundIntercept.class);
                                String key = annotation.value();
                                MethodAdvice advice = interceptorMap.get(key);
                                if (advice != null && advice.before != null) {
                                    advice.before.run();
                                }
                            }
                        },
                        () -> {
                            Method method = getCurrentMethod();
                            if (method != null) {
                                AroundIntercept annotation = method.getAnnotation(AroundIntercept.class);
                                String key = annotation.value();
                                MethodAdvice advice = interceptorMap.get(key);
                                if (advice != null && advice.after != null) {
                                    advice.after.run();
                                }
                            }
                        }
                ));

        return builder.build();
    }

    /**
     * Spring-specific interceptor that works with custom annotations
     */
    public static class SpringAnnotationInterceptor<T> {
        private final Class<T> targetClass;
        private final Map<Class<? extends Annotation>, MethodInterceptorHandler> interceptorHandlers = new HashMap<>(16, 0.75f);

        public SpringAnnotationInterceptor(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        /**
         * Register an interceptor handler for a specific annotation
         *
         * @param annotationClass The annotation class to intercept
         * @param handler         The handler to execute when the annotation is found
         * @return This interceptor for method chaining
         */
        public SpringAnnotationInterceptor<T> registerAnnotationHandler(
                Class<? extends Annotation> annotationClass,
                MethodInterceptorHandler handler) {
            interceptorHandlers.put(annotationClass, handler);
            return this;
        }

        /**
         * Create the proxy instance with all registered annotation handlers
         *
         * @return The proxied instance
         */
        public T createProxy() {
            InterceptorBuilder<T> builder = intercept(targetClass);

            // Register each annotation handler
            for (Map.Entry<Class<? extends Annotation>, MethodInterceptorHandler> entry :
                    interceptorHandlers.entrySet()) {

                Class<? extends Annotation> annotationClass = entry.getKey();
                MethodInterceptorHandler handler = entry.getValue();

                builder.interceptAnnotatedMethods(annotationClass,
                        new MethodAdvice(
                                () -> {
                                    Method method = getCurrentMethod();
                                    if (method != null) {
                                        Annotation annotation = method.getAnnotation(annotationClass);
                                        if (annotation != null) {
                                            handler.beforeMethod(method, annotation);
                                        }
                                    }
                                },
                                () -> {
                                    Method method = getCurrentMethod();
                                    if (method != null) {
                                        Annotation annotation = method.getAnnotation(annotationClass);
                                        if (annotation != null) {
                                            handler.afterMethod(method, annotation);
                                        }
                                    }
                                }
                        ));
            }

            return builder.build();
        }
    }

    /**
     * Interface for handling method interception based on annotations
     */
    public interface MethodInterceptorHandler {
        /**
         * Called before the intercepted method executes
         *
         * @param method     The method being intercepted
         * @param annotation The annotation instance on the method
         */
        void beforeMethod(Method method, Annotation annotation);

        /**
         * Called after the intercepted method executes
         *
         * @param method     The method being intercepted
         * @param annotation The annotation instance on the method
         */
        void afterMethod(Method method, Annotation annotation);
    }

    /**
     * Create a Spring-compatible annotation interceptor
     *
     * @param targetClass The class to proxy
     * @return A new SpringAnnotationInterceptor instance
     */
    public static <T> SpringAnnotationInterceptor<T> createSpringAnnotationInterceptor(Class<T> targetClass) {
        return new SpringAnnotationInterceptor<>(targetClass);
    }

    /**
     * Spring bean post-processor that automatically applies annotation interception
     */
    public static class AnnotationInterceptorBeanPostProcessor implements BeanPostProcessor {
        private final Map<Class<? extends Annotation>, MethodInterceptorHandler> registeredHandlers = new HashMap<>(16, 0.75f);

        /**
         * Register a handler for a specific annotation
         *
         * @param annotationClass The annotation class to intercept
         * @param handler         The handler to execute when the annotation is found
         * @return This post processor for method chaining
         */
        public AnnotationInterceptorBeanPostProcessor registerAnnotationHandler(
                Class<? extends Annotation> annotationClass,
                MethodInterceptorHandler handler) {
            registeredHandlers.put(annotationClass, handler);
            return this;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            Class<?> targetClass = bean.getClass();

            // Check if any methods in the class have our registered annotations
            boolean hasAnnotatedMethods = false;
            Method[] methods = targetClass.getDeclaredMethods();

            for (Method method : methods) {
                if (registeredHandlers.keySet().stream().anyMatch(method::isAnnotationPresent)) {
                    hasAnnotatedMethods = true;
                }
                if (hasAnnotatedMethods) {
                    break;
                }
            }

            // If we found annotated methods, create a proxy
            if (hasAnnotatedMethods) {
                SpringAnnotationInterceptor<?> interceptor = createSpringAnnotationInterceptor(targetClass);

                // Register all handlers
                for (Map.Entry<Class<? extends Annotation>, MethodInterceptorHandler> entry :
                        registeredHandlers.entrySet()) {
                    interceptor.registerAnnotationHandler(entry.getKey(), entry.getValue());
                }

                // Create and return the proxy
                return interceptor.createProxy();
            }

            // No annotated methods, return the original bean
            return bean;
        }
    }

    /**
     * Create a Spring bean post-processor that automatically intercepts annotated methods
     *
     * @return A new AnnotationInterceptorBeanPostProcessor
     */
    public static AnnotationInterceptorBeanPostProcessor createAnnotationInterceptorPostProcessor() {
        return new AnnotationInterceptorBeanPostProcessor();
    }

    // ThreadLocal to store current method being executed
    private static final ThreadLocal<Method> currentMethod = new ThreadLocal<>();

    // Helper method to get the current method
    private static Method getCurrentMethod() {
        return currentMethod.get();
    }

    // Helper method to set the current method
    private static void setCurrentMethod(Method method) {
        currentMethod.set(method);
    }
}
