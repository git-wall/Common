package org.app.security.security.chain;

import org.app.security.security.CorsConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({MainSecurityConfig.class, CorsConfig.class})
public @interface EnableSecurityChainLink {
}
