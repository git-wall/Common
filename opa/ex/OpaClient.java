package org.app.common.opa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class OpaClient {
    private final RestTemplate restTemplate;
    private final String opaUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public OpaClient(RestTemplate restTemplate, String opaUrl) {
        this.restTemplate = restTemplate;
        this.opaUrl = opaUrl;
    }
    
    public boolean checkPermission(String policyPath, Object input) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("input", objectMapper.valueToTree(input));
            
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            String url = String.format("%s/v1/data/%s", opaUrl, policyPath);
            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);
            
            return response != null && response.has("result") && response.get("result").asBoolean();
        } catch (Exception e) {
            // Log error and default to deny
            return false;
        }
    }
}