package org.app.common.controller;

import brave.Span;
import brave.Tracer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.app.common.contain.TagURL;
import org.app.common.interceptor.log.LogMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableHystrix
@RestController
@RequestMapping("${spring.application.name}")
@RequiredArgsConstructor
public class PingController {

    @Value("${spring.application.name}")
    private String application;

    private final Tracer tracer;

    private static final String ERROR_MESSAGE = "Request fails. It takes long time to response";

    @LogMonitor(type = LogMonitor.LogType.GRAYLOG)
    @GetMapping(TagURL.PING)
    @HystrixCommand(fallbackMethod = "fallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "25")
            }
    )
    public String ping() {
        Span pingSpan = tracer.nextSpan().name("ping chilling");
        try (Tracer.SpanInScope scope = tracer.withSpanInScope(pingSpan.start())) {
            return "Hello from " + application + " !";
        } finally {
            pingSpan.finish();
        }
    }

    private String fallback() {
        return ERROR_MESSAGE;
    }
}
