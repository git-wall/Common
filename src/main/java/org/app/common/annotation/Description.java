package org.app.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation for description of type include class, method, field etc
 * */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {
    String detail() default "";
}
