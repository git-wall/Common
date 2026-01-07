package org.app.common.condition;

import lombok.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnLeaderCondition implements Condition {

    @Override
    public boolean matches(ConditionContext ctx, @NonNull AnnotatedTypeMetadata md) {
        return Boolean.parseBoolean(
            ctx.getEnvironment().getProperty("node.is-leader", "false")
        );
    }
}
