package org.app.common.condition;

import lombok.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Use-case:
 * Condition to check if the current node is a worker node.
 * This can be used to conditionally load beans or configurations
 * that should only be active on worker nodes.
 * <pre>{@code
 *     @Conditional(OnWorkerNodeCondition.class)
 *     public class AsyncWorkerConfig {}
 * }</pre>
 * */
///
///
public class OnWorkerNodeCondition implements Condition {

    @Override
    public boolean matches(ConditionContext ctx, @NonNull AnnotatedTypeMetadata md) {
        return "worker".equals(
            ctx.getEnvironment().getProperty("node.role")
        );
    }
}

