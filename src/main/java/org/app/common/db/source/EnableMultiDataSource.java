package org.app.common.db.source;

import org.app.common.db.source.config.SourceAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable multi-datasource support
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SourceAutoConfiguration.class)
public @interface EnableMultiDataSource {

    SourceConfigType configType() default SourceConfigType.PROPERTIES;
}
