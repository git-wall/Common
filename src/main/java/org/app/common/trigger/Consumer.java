package org.app.common.trigger;

public @interface Consumer {
    String group() default "";
}
