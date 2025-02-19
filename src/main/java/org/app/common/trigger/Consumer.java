package org.app.common.trigger;

public @interface Consumer {
    String value() default "";
    String group() default "";
}
