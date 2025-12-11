package org.app.common.opa;

import org.app.common.client.http.WebClient;
import org.app.common.constant.JType;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class OpaClient {
    private final WebClient webClient;
    private final OpaProperties opaProperties;
    private static final String RESULT = "result";

    public OpaClient(WebClient webClient, OpaProperties opaProperties) {
        this.webClient = webClient;
        this.opaProperties = opaProperties;
    }

    public boolean checkPermission(Object body) {
        try {
            Map<String, Object> response = webClient.read(HttpMethod.POST, body, opaProperties.getUri(), JType.MAP_STR_OBJ);
            return response != null && response.containsKey(RESULT) && Boolean.parseBoolean(response.get(RESULT).toString());
        } catch (Exception e) {
            return false;
        }
    }
}
