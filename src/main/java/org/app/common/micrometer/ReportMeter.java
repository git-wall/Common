package org.app.common.micrometer;

public interface ReportMeter {
    void timer(String name, Runnable runnable, String tags);

    void counter(String name, String tags);

    void gauge(String name);
}
