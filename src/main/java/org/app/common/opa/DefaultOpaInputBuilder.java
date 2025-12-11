package org.app.common.opa;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultOpaInputBuilder implements OpaInputBuilder {

    @Override
    public Map<String, Object> buildInput(HttpServletRequest request, Authentication authentication) {
        // User info
        Map<String, Object> user = getUser(authentication);

        // Request info
        Map<String, Object> requestInfo = getRequestInfo(request);

        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames()
            .asIterator()
            .forEachRemaining(headerName -> headers.put(headerName, request.getHeader(headerName)));
        requestInfo.put("headers", headers);

        Map<String, Object> input = new HashMap<>(2);
        input.put("user", user);
        input.put("request", requestInfo);

        return input;
    }

    private static @NotNull Map<String, Object> getRequestInfo(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>(3);
        requestInfo.put("method", request.getMethod());
        requestInfo.put("path", request.getRequestURI());
        return requestInfo;
    }

    private static @NotNull Map<String, Object> getUser(Authentication authentication) {
        Map<String, Object> user = new HashMap<>(3);
        user.put("id", authentication.getName());
        user.put("roles", authentication.getAuthorities().stream()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toList()));
        user.put("authenticated", authentication.isAuthenticated());
        return user;
    }
}

