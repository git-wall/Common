package org.app.common.db.transaction;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionSupportConfiguration.class)
public @interface EnableTransactionSupport {
    int defaultTimeout() default 30;

    class Key {
        protected static final String DEFAULT_TX = "DEFAULT_TX";
        protected static final String DEFAULT_RETRY = "DEFAULT_RETRY";
    }
}
