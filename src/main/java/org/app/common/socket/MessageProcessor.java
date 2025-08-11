package org.app.common.socket;

import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.JacksonUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class MessageProcessor {

    private final WebSocketSessionManager sessionManager;

    public MessageProcessor(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void process(WebSocketSession session, String payload) {
        try {
            WebSocketMessage message = JacksonUtils.readValue(payload, WebSocketMessage.class);

            switch (message.getType()) {
                case "SUBSCRIBE":
                    sessionManager.subscribe(session.getId(), message.getTopic());
                    break;
                case "BROADCAST":
                    sessionManager.broadcast(message.getTopic(), message.getData());
                    break;
                case "DIRECT":
                    sessionManager.sendToSession(message.getTarget(), message.getData());
                    break;
                default:
                    handleCustomMessage(session, message);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    protected void handleCustomMessage(WebSocketSession session, WebSocketMessage message) {
        // Override in subclasses for custom logic
    }
}
