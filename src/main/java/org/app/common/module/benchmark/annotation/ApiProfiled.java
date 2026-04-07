package org.app.common.module.benchmark.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu API method cần profile performance
 * Tự động thu thập: time, memory, call count cho toàn bộ call tree
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiProfiled {

    /**
     * Tên API để group kết quả (VD: "OrderAPI", "PaymentAPI")
     * Nếu để trống sẽ dùng method signature
     */
    String value() default "";

    /**
     * Cảnh báo nếu time vượt ngưỡng (ms)
     */
    long warnTimeMs() default 500;

    /**
     * Cảnh báo nếu memory vượt ngưỡng (KB)
     */
    long warnMemKb() default 1024;
}
