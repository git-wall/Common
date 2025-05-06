package org.app.common.thread;

import org.app.common.annotation.Description;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Description(detail = "This class for collect class have injection to register thread to class RunnableProvider")
public @interface AutoRun {
    String detail() default "This class is auto-registered for execution";
}
