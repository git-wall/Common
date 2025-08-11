package org.app.common.socket;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSocket
@Slf4j
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandlerFactory handlerFactory;

    @Value("${websocket.endpoints:}")
    private String[] endpoints;

    @Value("${websocket.path.prefix:/}")
    private String pathPrefix;

    @Value("${websocket.allowed.origins:*}")
    private String[] allowedOrigins;

    @Value("${websocket.endpoint.configs:}")
    private String[] endpointConfigs;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Parse endpoint-specific configurations
        Map<String, EndpointConfig> configMap = parseEndpointConfigs();

        // Dynamic endpoint registration
        for (String endpoint : endpoints) {
            String path = normalizePath(pathPrefix) + endpoint;
            EndpointConfig config = configMap.getOrDefault(endpoint, new EndpointConfig());

            log.info("Registering WebSocket handler for endpoint: {} at path: {}", endpoint, path);

            registry.addHandler(handlerFactory.createHandler(endpoint), path)
                    .setAllowedOrigins(config.getAllowedOrigins(allowedOrigins));
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "/";
        }
        return path.endsWith("/") ? path : path + "/";
    }

    private Map<String, EndpointConfig> parseEndpointConfigs() {
        Map<String, EndpointConfig> configMap = new HashMap<>();

        for (String config : endpointConfigs) {
            if (!StringUtils.hasText(config)) {
                continue;
            }

            String[] parts = config.split(":");
            if (parts.length < 2) {
                log.warn("Invalid endpoint config format: {}", config);
                continue;
            }

            String endpoint = parts[0].trim();
            String[] options = parts[1].split(",");

            EndpointConfig endpointConfig = new EndpointConfig();
            for (String option : options) {
                String[] keyValue = option.split("=");
                if (keyValue.length != 2) {
                    continue;
                }

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if ("origins".equals(key)) {
                    endpointConfig.setAllowedOrigins(value.split("\\|"));
                }
            }

            configMap.put(endpoint, endpointConfig);
        }

        return configMap;
    }

    @Setter
    private static class EndpointConfig {
        private String[] allowedOrigins;

        public String[] getAllowedOrigins(String[] defaultOrigins) {
            return allowedOrigins != null ? allowedOrigins : defaultOrigins;
        }

    }
}
