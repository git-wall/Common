package org.app.security.shield;

import org.app.security.shield.event.AuthEventBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// send authentication events to message broker
// for auditing and monitoring, security analysis
// such as tracking login successes and failures
// and detecting suspicious activities like hacking attempts
@Component
public class SecurityEventListener {

    private final AuthEventSender sender;
    private final HttpServletRequest request;
    private final String serviceName;

    public SecurityEventListener(
        AuthEventSender sender,
        HttpServletRequest request,
        @Value("${spring.application.name}") String serviceName
    ) {
        this.sender = sender;
        this.request = request;
        this.serviceName = serviceName;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwt = (JwtAuthenticationToken) event.getAuthentication();
            sender.send(AuthEventBuilder.success(jwt, request, serviceName));
        }
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        sender.send(AuthEventBuilder.failure(event.getException(), request, serviceName));
    }
}
