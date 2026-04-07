package org.app.common.context;

import org.app.common.interceptor.context.AuthContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

// dành cho Async / @Async / Executor (rất quan trọng)
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public @NotNull Runnable decorate(@NotNull Runnable r) {
        AuthContext auth = AuthContextHolder.get();
        Map<String, String> mdc = MDC.getCopyOfContextMap();

        return () -> {
            try {
                AuthContextHolder.set(auth);
                MDC.setContextMap(mdc);
                r.run();
            } finally {
                AuthContextHolder.clear();
                MDC.clear();
            }
        };
    }
}
