package org.app.common.module.call_center.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthInfo {
    private String type;
    private String tokenEndpoint;
    private String method;
    private Map<String, Object> body;
    private String tokenField;
    private String header;
    private String prefix;
    
    // Runtime fields - not stored in definition
    private LocalDateTime expiresAt;
    private String currentToken;
}