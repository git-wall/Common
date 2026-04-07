package org.app.aop.interceptor.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InterceptorLog {
    boolean trace() default false;
    boolean root() default false;
    // depth in stack trace when have error
    int depth() default 0;
}
