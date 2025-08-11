package org.app.common.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        removeFromAllTopics(session.getId());
    }

    public void subscribe(String sessionId, String topic) {
        topicSubscriptions.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    public void broadcast(String topic, String message) {
        Set<String> subscribers = topicSubscriptions.get(topic);
        if (subscribers != null) {
            subscribers.forEach(sessionId -> sendToSession(sessionId, message));
        }
    }

    public void sendToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    private void removeFromAllTopics(String sessionId) {
        topicSubscriptions.values().forEach(subscribers -> subscribers.remove(sessionId));
    }
}

