package org.app.common.kafka.single;

import org.app.common.kafka.single.config.KafkaConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Import(KafkaConfig.class)
public @interface EnableKafkaAutoConfig {
}
