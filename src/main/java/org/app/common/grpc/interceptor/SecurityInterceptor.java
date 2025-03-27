package org.app.common.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Interceptor for handling authentication and authorization in gRPC calls.
 */
@Slf4j
@Component
public class SecurityInterceptor implements ServerInterceptor {
    
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Value("${grpc.auth.enabled:false}")
    private boolean authEnabled;
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, 
            Metadata headers, 
            ServerCallHandler<ReqT, RespT> next) {
        
        if (!authEnabled) {
            return next.startCall(call, headers);
        }
        
        // Extract the auth token from headers
        String token = extractToken(headers);
        
        if (token == null) {
            log.warn("Missing authentication token for method: {}", 
                    call.getMethodDescriptor().getFullMethodName());
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Authentication token is missing"), 
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
        
        try {
            // Validate the token (implement your validation logic here)
            if (!validateToken(token, call.getMethodDescriptor().getFullMethodName())) {
                log.warn("Invalid authentication token for method: {}", 
                        call.getMethodDescriptor().getFullMethodName());
                call.close(Status.PERMISSION_DENIED
                        .withDescription("Permission denied"), 
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }
            
            // If authentication passes, proceed with the call
            return next.startCall(call, headers);
        } catch (Exception e) {
            log.error("Error during authentication", e);
            call.close(Status.INTERNAL
                    .withDescription("Internal authentication error"), 
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
    }
    
    private String extractToken(Metadata headers) {
        String value = headers.get(Metadata.Key.of(AUTH_HEADER, Metadata.ASCII_STRING_MARSHALLER));
        if (value == null || !value.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return value.substring(BEARER_PREFIX.length());
    }
    
    private boolean validateToken(String token, String methodName) {
        // Implement your token validation logic here
        // This could involve JWT validation, checking against a database, etc.
        // For now, we'll just return true as a placeholder
        return true;
    }
}