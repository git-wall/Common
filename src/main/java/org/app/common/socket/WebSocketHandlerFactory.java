package org.app.common.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.SpringContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandlerFactory {

    private final GenericWebSocketHandler genericHandler;

    public WebSocketHandler createHandler(String endpoint) {
        // Try to find a specific handler for this endpoint
        try {
            // First try with exact name
            String beanName = endpoint + "WebSocketHandler";
            if (SpringContext.containsBean(beanName)) {
                log.info("Using specific handler for endpoint: {}", endpoint);
                return SpringContext.getBean(beanName, WebSocketHandler.class);
            }

            // Then try with capitalized name
            String capitalizedEndpoint = endpoint.substring(0, 1).toUpperCase() + endpoint.substring(1);
            beanName = capitalizedEndpoint + "WebSocketHandler";
            if (SpringContext.containsBean(beanName)) {
                log.info("Using specific handler for endpoint: {}", endpoint);
                return SpringContext.getBean(beanName, WebSocketHandler.class);
            }

            // Fall back to generic handler
            log.info("No specific handler found for endpoint: {}, using generic handler", endpoint);
            return genericHandler;
        } catch (Exception e) {
            log.warn("Error finding handler for endpoint: {}, falling back to generic handler", endpoint, e);
            return genericHandler;
        }
    }
}
