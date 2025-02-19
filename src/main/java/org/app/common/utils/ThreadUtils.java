package org.app.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.Predicate;

@Slf4j
public class ThreadUtils {

    /**
     * Logs the current thread's stack trace.
     */
    public static void logStackTrace(String sourceBaseName) {
        // Get the current thread's stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // Build the method flow string
        StringBuilder methodFlow = new StringBuilder("Method Flow: ");
        boolean isFirst = true;

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();

            // Exclude internal Spring/Java frames
            if (    className.startsWith(sourceBaseName) &&
                    !className.startsWith("org.springframework") &&
                    !className.startsWith("java.lang") &&
                    !methodName.equals("getStackTrace") && // Exclude utility method itself
                    !methodName.equals("logMethodFlow")) { // Exclude utility method itself

                if (!isFirst) {
                    methodFlow.append(" -> ");
                }
                methodFlow.append(methodName);
                isFirst = false;
            }
        }

        // Log the method flow
        log.info(methodFlow.toString());
    }

    public static void logFilterStackTrace(String sourceBaseName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Arrays.stream(stackTrace)
                .filter(filterStackTraceElement(sourceBaseName))
                .forEach(element -> log.info("  at {}", element));
    }

    private static Predicate<StackTraceElement> filterStackTraceElement(String sourceBaseName) {
        return element ->
                isRelevantClass(element.getClassName(), sourceBaseName)
                        && !isExcludedMethod(element.getMethodName());
    }

    private static boolean isRelevantClass(String className, String sourceBaseName) {
        return className.startsWith(sourceBaseName)
                && !className.startsWith("org.springframework")
                && !className.startsWith("java.lang");
    }

    private static boolean isExcludedMethod(String methodName) {
        return methodName.equals("getStackTrace") || methodName.equals("logMethodFlow");
    }
}
