package org.app.concurrent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
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
            Method method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            method.setAccessible(true);
            Object o = method.invoke(null);
            return (ExecutorService) o;
        } catch (Throwable e) {
            return null; // Java < 21
        }
    }
}

