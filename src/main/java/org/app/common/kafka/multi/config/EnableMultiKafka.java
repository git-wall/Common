package org.app.common.kafka.multi.config;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MultiKafkaConfig.class)
public @interface EnableMultiKafka {
}
