package org.app.common.interceptor.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InterceptorLog {
    LogType type() default LogType.GRAYLOG;

    enum LogType {
        GRAYLOG,
        KAFKA,
        ELK,
        NIFI,
        CLICKHOUSE,
    }
}
