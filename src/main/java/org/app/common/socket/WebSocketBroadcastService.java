package org.app.common.socket;

import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.JacksonUtils;
import org.springframework.stereotype.Service;

// Service for broadcasting
@Service
@Slf4j
public class WebSocketBroadcastService {

    private final WebSocketSessionManager sessionManager;

    public WebSocketBroadcastService(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void broadcastToTopic(String topic, Object data) {
        try {
            String message = JacksonUtils.writeValueAsString(data);
            sessionManager.broadcast(topic, message);
        } catch (Exception e) {
            log.error("Failed to broadcast to topic {}: {}", topic, e.getMessage());
        }
    }

    public void sendToUser(String sessionId, Object data) {
        try {
            String message = JacksonUtils.writeValueAsString(data);
            sessionManager.sendToSession(sessionId, message);
        } catch (Exception e) {
            log.error("Failed to send to user {}: {}", sessionId, e.getMessage());
        }
    }
}