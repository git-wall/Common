package org.app.common.metrics;

public interface ReportMeter {
    void timer(String name, Runnable runnable, String tags);

    void counter(String name, String tags);

    void gauge(String name);
}
