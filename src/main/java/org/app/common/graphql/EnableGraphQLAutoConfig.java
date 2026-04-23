<<<<<<<< HEAD:src/main/java/org/app/common/graphql/EnableGraphQLAutoConfig.java
package org.app.common.graphql;
========
package org.app.security.security.chain;
>>>>>>>> dev:security/src/main/java/org/app/security/security/chain/EnableSecurityChainLink.java

import org.app.security.security.CorsConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
<<<<<<<< HEAD:src/main/java/org/app/common/graphql/EnableGraphQLAutoConfig.java
@Retention(RetentionPolicy.RUNTIME)
@Import(GraphQLAutoConfig.class)
public @interface EnableGraphQLAutoConfig {
========
@Import({MainSecurityConfig.class, CorsConfig.class})
public @interface EnableSecurityChainLink {
>>>>>>>> dev:security/src/main/java/org/app/security/security/chain/EnableSecurityChainLink.java
}
