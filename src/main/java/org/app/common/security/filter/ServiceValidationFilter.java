package org.app.common.security.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// ============================================
// Service Validation Filter (cho Internal API)
// ============================================
@Slf4j
@RequiredArgsConstructor
public class ServiceValidationFilter extends OncePerRequestFilter {

    private final List<String> allowedServices;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check pass token -> 401
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new InsufficientAuthenticationException("Missing authentication");
            }

            // Check can be handler → 403
            String serviceId = request.getHeader("X-Service-Id");
            if (!allowedServices.contains(serviceId)) {
                throw new AccessDeniedException("Unauthorized internal service access attempt");
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException | AccessDeniedException e) {
            SecurityContextHolder.clearContext();
            throw e;
        }
    }
}
