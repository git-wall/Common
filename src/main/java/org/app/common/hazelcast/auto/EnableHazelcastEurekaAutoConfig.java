package org.app.common.hazelcast.auto;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HazelcastConfiguration.class)
public @interface EnableHazelcastEurekaAutoConfig {
}
