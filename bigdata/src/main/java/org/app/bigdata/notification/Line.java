package org.app.bigdata.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.app.http.core.ClientUtils;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

@Data
public class Line {
    private final NotificationInfo notificationInfo;
    private final HttpClient httpClient;

    public Line(NotificationInfo notificationInfo, HttpClient httpClient) {
        this.notificationInfo = notificationInfo;
        this.httpClient = httpClient;
    }

    public void notify(String message) {
        NotifyMessage notifyMessage = new NotifyMessage(notificationInfo.getTopic(), message);

        ClientUtils.Async.fireAndForget(
            httpClient,
            notifyMessage,
            notificationInfo.getTargetUrl(),
            notificationInfo.getTokenAuth()
        );
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
