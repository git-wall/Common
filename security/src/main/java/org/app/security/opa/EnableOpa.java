<<<<<<<< HEAD:security/src/main/java/org/app/security/security/opa/EnableOpa.java
package org.app.security.security.opa;
========
package org.app.security.opa;
>>>>>>>> dev:security/src/main/java/org/app/security/opa/EnableOpa.java

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({OpaAutoConfiguration.class})
public @interface EnableOpa {
}
