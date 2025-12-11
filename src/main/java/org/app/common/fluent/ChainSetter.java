package org.app.common.fluent;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ChainSetter {
    /**
     * Whether to generate fluent setters (fieldName() instead of setFieldName())
     * Default false to maintain Jackson compatibility
     */
    boolean fluent() default false;

    /**
     * Prefix for chaining methods when fluent=false
     * Default "with" creates withFieldName() methods
     */
    String prefix() default "with";

    /**
     * Generate standard setters that return this instead of void
     * When true, replaces standard setters with chaining versions
     */
    boolean chain() default false;

    boolean lombok() default true; // Work with Lombok
}

