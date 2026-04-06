package org.app.bigdata.notification;

import lombok.extern.slf4j.Slf4j;
import org.app.jackson.JacksonUtils;

import java.net.http.HttpClient;

@Slf4j
public class SnapShot {

    /**
     * Sends an error message to the specified Line notification channel.
     *
     * @param ex       The exception that occurred.
     * @param msg      A custom message to include in the notification.
     * @param notificationInfo Information about the Line notification channel.
     */
    public static void toLine(Exception ex, Object msg, NotificationInfo notificationInfo, HttpClient client) {
        Line line = new Line(notificationInfo, client);

        StackTraceElement[] stack = ex.getStackTrace();
        StringBuilder message = new StringBuilder();

        String request = String.format("Request: %s", JacksonUtils.writeValueAsString(msg));
        message.append(request)
                .append("Exception Message: ")
                .append(ex)
                .append("\n");
        for (StackTraceElement stt : stack) {
            message.append(stt.getClassName())
                    .append(".")
                    .append(stt.getMethodName())
                    .append("(")
                    .append(stt.getFileName())
                    .append(":")
                    .append(stt.getLineNumber())
                    .append(")");
        }

        line.notify(message.toString());
    }
}
