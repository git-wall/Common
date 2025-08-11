package org.app.common.support;

import lombok.extern.slf4j.Slf4j;
import org.app.common.notification.Line;
import org.app.common.notification.NotificationInfo;

@Slf4j
public class SnapShot {

    /**
     * Sends an error message to the specified Line notification channel.
     *
     * @param ex       The exception that occurred.
     * @param msg      A custom message to include in the notification.
     * @param notificationInfo Information about the Line notification channel.
     */
    public static void exceptionToLine(Exception ex, String msg, NotificationInfo notificationInfo) {
        Line line = new Line(notificationInfo);

        StackTraceElement[] stack = ex.getStackTrace();
        StringBuilder message = new StringBuilder();

        message.append(msg)
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
