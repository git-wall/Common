package org.app.common.component;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

// tránh heapdump / threaddump full
@Component
@Endpoint(id = "thread-summary")
public class ThreadSummaryEndpoint {
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    @ReadOperation
    public Map<String, Object> threads() {
        return Map.of(
            "threadCount", bean.getThreadCount(),
            "daemonCount", bean.getDaemonThreadCount(),
            "peakCount", bean.getPeakThreadCount()
        );
    }
}
