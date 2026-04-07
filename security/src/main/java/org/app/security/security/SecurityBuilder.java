package org.app.security.security;

import lombok.extern.slf4j.Slf4j;
import org.app.jackson.JacksonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SecurityBuilder {

    public static void authEntryPointHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        try {
            ApiResponse<?> apiResponse = ApiResponse.error(
                String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                "Unauthorized: " + ex.getMessage()
            );

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JacksonUtils.writeValueAsString(apiResponse));
            response.flushBuffer();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void accessDeniedHandler(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        try {
            ApiResponse<?> apiResponse = ApiResponse.error(
                String.valueOf(HttpStatus.FORBIDDEN.value()),
                "Unauthorized: " + ex.getMessage()
            );

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JacksonUtils.writeValueAsString(apiResponse));
            response.flushBuffer();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
