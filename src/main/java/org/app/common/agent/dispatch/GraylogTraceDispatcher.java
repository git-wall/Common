package org.app.common.agent.dispatch;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import de.siegmar.logbackgelf.GelfEncoder;
import de.siegmar.logbackgelf.GelfUdpAppender;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GraylogTraceDispatcher implements TraceDispatcher {
    private final Logger logger;
    private final String facility;

    public GraylogTraceDispatcher(String host, int port, String facility) {
        this.facility = facility;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Add status listener
        OnConsoleStatusListener statusListener = new OnConsoleStatusListener();
        statusListener.setContext(context);
        context.getStatusManager().add(statusListener);

        // === STDOUT Appender ===
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        consoleEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        consoleEncoder.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();

        // === GELF Appender ===
        GelfEncoder gelfEncoder = new GelfEncoder();
        gelfEncoder.setContext(context);
        gelfEncoder.setOriginHost("localhost");
        gelfEncoder.setIncludeRawMessage(false);
        gelfEncoder.setIncludeMarker(true);
        gelfEncoder.setIncludeMdcData(true);
        gelfEncoder.setIncludeCallerData(false);
        gelfEncoder.setIncludeRootCauseData(false);
        gelfEncoder.setIncludeLevelName(true);

        // Short pattern layout
        ch.qos.logback.classic.PatternLayout shortPattern = new ch.qos.logback.classic.PatternLayout();
        shortPattern.setContext(context);
        shortPattern.setPattern("%logger{35} - %msg%n");
        shortPattern.start();
        gelfEncoder.setShortPatternLayout(shortPattern);

        // Full pattern layout
        ch.qos.logback.classic.PatternLayout fullPattern = new ch.qos.logback.classic.PatternLayout();
        fullPattern.setContext(context);
        fullPattern.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{35} - %msg%n");
        fullPattern.start();
        gelfEncoder.setFullPatternLayout(fullPattern);

        gelfEncoder.start();

        GelfUdpAppender gelfAppender = new GelfUdpAppender();
        gelfAppender.setContext(context);
        gelfAppender.setGraylogHost("localhost");
        gelfAppender.setGraylogPort(12201);
        gelfAppender.setMaxChunkSize(508);
        gelfAppender.setUseCompression(true);
        gelfAppender.setEncoder(gelfEncoder);
        gelfAppender.start();

        // Create a dedicated logger for this dispatcher
        logger = context.getLogger(GraylogTraceDispatcher.class.getName() + "." + facility);
        logger.addAppender(gelfAppender);
        logger.addAppender(consoleAppender);
    }

    @Override
    public void dispatch(String traceId, Map<String, Object> context) {
        try {
            // Put all context data into MDC for GELF logging
            org.slf4j.MDC.put("traceId", traceId);
            context.forEach((key, value) -> {
                if (value != null) {
                    org.slf4j.MDC.put(key, value.toString());
                }
            });

            // Log the message with all context in MDC
            logger.info("Trace event from {}: {}", facility, traceId);
        } catch (Exception e) {
            logger.error("Failed to send trace to Graylog", e);
        } finally {
            // Clean up MDC
            org.slf4j.MDC.clear();
        }
    }
}
