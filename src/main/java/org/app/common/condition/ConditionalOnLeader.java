package org.app.common.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use-case:
 * Only 1 pod run job for not duplicate job (scheduler, cleanup, sync)
 * */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnLeaderCondition.class)
public @interface ConditionalOnLeader {
}

