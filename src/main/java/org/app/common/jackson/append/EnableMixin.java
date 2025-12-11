package org.app.common.jackson.append;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Auto append requestId, traceId, timestamp to all responses
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MixinConfig.class)
public @interface EnableMixin {
    Class<JsonMixin> mixin();
}
