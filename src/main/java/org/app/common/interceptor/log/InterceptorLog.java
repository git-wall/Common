package org.app.common.interceptor.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorLog {
    LogType type() default LogType.LOG;

    enum LogType {
        GRAYLOG,
        KAFKA,
        ELK,
        NIFI,
        LOG
    }
}
