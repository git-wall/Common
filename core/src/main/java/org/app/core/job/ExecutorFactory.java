package org.app.core.job;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.core.utils.ClassUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorFactory {

    public static ExecutorService create() {
        ExecutorService vt = tryCreateVirtualThreadExecutor();
        if (vt != null) {
            return vt;
        }

        int cpu = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(cpu);
    }

    private static ExecutorService tryCreateVirtualThreadExecutor() {
        try {
            return ClassUtils
                .invokeMethod(Executors.class, "newVirtualThreadPerTaskExecutor", ExecutorService.class);
        } catch (Throwable e) {
            return null; // Java < 21
        }
    }
}

