package org.app.common.cache.ignite;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables Apache Ignite integration in a Spring Boot application with
 * comprehensive features including OLTP, HTAP analytics, distributed caching,
 * microservices data fabric, and machine learning capabilities.
 *
 * Simply add this annotation to any @Configuration class to enable Apache Ignite
 * with autoconfiguration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(IgniteAutoConfiguration.class)
public @interface EnableApacheIgnite {

    /**
     * Enables/disables OLTP system support
     */
    boolean enableOltp() default true;

    /**
     * Enables/disables real-time analytics (HTAP) support
     */
    boolean enableAnalytics() default true;

    /**
     * Enables/disables distributed caching capabilities
     */
    boolean enableCaching() default true;

    /**
     * Enables/disables microservices data fabric support
     */
    boolean enableMicroservices() default true;

    /**
     * Enables/disables machine learning features
     */
    boolean enableMachineLearning() default false;

    /**
     * Enables/disables persistent storage
     */
    boolean persistentStorage() default false;
}