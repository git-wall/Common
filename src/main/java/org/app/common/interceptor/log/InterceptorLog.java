package org.app.common.interceptor.log;

import java.lang.annotation.*;
import java.util.function.Supplier;

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

    // Nested helper interface check
    interface Check {
        static boolean isKafka(LogType logType) {
            return LogType.KAFKA.equals(logType);
        }
    }

    // Nested helper interface default
    interface Default {
        static Supplier<InterceptorLog> instance() {
            return () -> new InterceptorLog() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return InterceptorLog.class;
                }
                @Override
                public LogType type() {
                    return LogType.GRAYLOG;
                }
            };
        }
    }
}
