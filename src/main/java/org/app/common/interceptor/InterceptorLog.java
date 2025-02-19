package org.app.common.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorLog {
    LogType type() default LogType.GRAYLOG;

    enum LogType {
        GRAYLOG,
        KAFKA,
        ELK,
        NONE
    }
}
