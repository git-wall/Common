package org.app.common.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.app.common.client.http.HttpClientUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.HashMap;
import java.util.Map;

@Data
public class Line {
    private final NotificationInfo notificationInfo;

    public Line(NotificationInfo notificationInfo) {
        this.notificationInfo = notificationInfo;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void notify(String message) {
        NotifyMessage notifyMessage = new NotifyMessage(notificationInfo.getTopic(), message);

        HttpClientUtils.call(notifyMessage, notificationInfo.getTargetUrl(), notificationInfo.getTokenAuth()).statusCode();
    }

    @Data
    public static class NotifyMessage {
        @JsonProperty("channel_id")
        private String channel_id;
        @JsonProperty("message")
        private String message;
        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        public NotifyMessage(String channel, String message) {
            this.channel_id = channel;
            this.message = message;
            this.metadata = new HashMap<>();
            Map<String, Object> priority = new HashMap<>();
            priority.put("priority", "standard");
            priority.put("requested_ack", false);
            priority.put("persistent_notifications", false);
            metadata.put("priority", priority);
        }
    }
}
