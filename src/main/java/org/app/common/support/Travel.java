package org.app.common.support;

import io.vavr.Tuple2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.Callable;

@Slf4j
public class Travel {

    private Travel() {
    }

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

    /**
     * @param callable The callable to be executed
     * &#064;Returns Tuple2 where _1 is the result and _2 is the time taken in milliseconds
     * */
    @SneakyThrows
    public static <T> Tuple2<T, Long> result$timer(Callable<T> callable) {
        var s = System.currentTimeMillis();
        T result = callable.call();
        return new Tuple2<>(result, System.currentTimeMillis() - s);
    }

    @SneakyThrows
    public static Object process(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

    private static void logTime(long e, long s) {
        log.info("Time taken (ms): {}", e - s);
    }
}
