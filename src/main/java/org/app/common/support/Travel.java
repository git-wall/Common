package org.app.common.support;

import io.vavr.Tuple2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.Callable;

@Slf4j
public class Travel {

    @SneakyThrows
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
        var s = System.currentTimeMillis();
        T result = callable.call();
        logTime(System.currentTimeMillis(), s);
        return result;
    }

    @SneakyThrows
    public static <T> Tuple2<T, Long> tuple$timer(Callable<T> callable) {
        var s = System.currentTimeMillis();
        T result = callable.call();
        return new Tuple2<>(result, System.currentTimeMillis() - s);
    }

    @SneakyThrows
    public static Object process(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

    private static void logTime(long e, long s) {
        log.info("time: {}", e - s);
    }
}
