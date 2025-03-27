package org.app.common.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interceptor for validating gRPC request messages.
 * Uses Java Bean Validation (JSR 380) to validate request objects.
 */
@Slf4j
@Component
public class ValidationInterceptor implements ServerInterceptor {
    
    private final Validator validator;
    
    public ValidationInterceptor() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {
            
            @Override
            public void onMessage(ReqT message) {
                // Validate the message
                Set<ConstraintViolation<ReqT>> violations = validator.validate(message);
                
                if (!violations.isEmpty()) {
                    // If there are validation errors, abort the call
                    String errorMessage = violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .collect(Collectors.joining(", "));
                    
                    log.error("gRPC request validation failed: {}", errorMessage);
                    
                    call.close(Status.INVALID_ARGUMENT
                            .withDescription("Validation failed: " + errorMessage), 
                            new Metadata());
                    return;
                }
                
                // If validation passes, proceed with the call
                super.onMessage(message);
            }
        };
    }
}