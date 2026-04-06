package org.app.message.kafka.single;

import org.app.message.kafka.single.config.KafkaTemplateConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Import(KafkaTemplateConfig.class)
public @interface EnableKafkaAutoConfig {
}
