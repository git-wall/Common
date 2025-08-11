package org.app.common.opa;

import com.fasterxml.jackson.databind.JsonNode;
import org.app.common.client.rest.BodyUtils;
import org.app.common.client.rest.HeaderUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

public class OpaClient {
    private final RestTemplate restTemplate;
    private final String opaUrl;
    
    public OpaClient(RestTemplate restTemplate, String opaUrl) {
        this.restTemplate = restTemplate;
        this.opaUrl = opaUrl;
    }
    
    public boolean checkPermission(String policyPath, Object input) {
        try {
            var headers = HeaderUtils.createHeaders();
            var body = BodyUtils.getBody(input);
            var request = new HttpEntity<>(body, headers);
            
            String url = String.format(opaUrl, policyPath);
            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);
            
            return response != null && response.has("result") && response.get("result").asBoolean();
        } catch (Exception e) {
            // Log error and default to deny
            return false;
        }
    }
}