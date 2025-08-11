package org.app.common.notification;

public enum NotificationType {
    LINE("LINE"),
    EMAIL("EMAIL"),
    SMS("SMS"),
    WEBHOOK("WEBHOOK"),
    TELEGRAM("TELEGRAM");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
