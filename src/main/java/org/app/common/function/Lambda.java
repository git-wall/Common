package org.app.common.function;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class Lambda {

    public static void around(Runnable runnable) {
        runnable.run();
    }

    @SneakyThrows
    public static <T> T around(Callable<T> callable) {
        return callable.call();
    }

    public static void timer(Runnable runnable) {
        long s = System.currentTimeMillis();
        runnable.run();
        long e = System.currentTimeMillis();
        logTime(e, s);
    }

    @SneakyThrows
    public static <T> T timer(Callable<T> callable) {
        long s = System.currentTimeMillis();
        T result = callable.call();
        long e = System.currentTimeMillis();
        logTime(e, s);
        return result;
    }

    private static void logTime(long e, long s) {
        log.info("time: {}", e - s);
    }
}
