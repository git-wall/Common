package org.app.common.opa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OpaAuthorizationFilter extends OncePerRequestFilter {
    
    private final OpaClient opaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public OpaAuthorizationFilter(OpaClient opaClient) {
        this.opaClient = opaClient;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> input = buildOpaInput(request, authentication);
            
            boolean allowed = opaClient.checkPermission("example/allow", input);
            
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied by policy");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Map<String, Object> buildOpaInput(HttpServletRequest request, Authentication authentication) {
        Map<String, Object> input = new HashMap<>();
        
        // User information
        Map<String, Object> user = new HashMap<>();
        user.put("id", authentication.getName());
        user.put("roles", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList()));
        user.put("authenticated", authentication.isAuthenticated());
        
        // Request information
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("path", request.getRequestURI());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            headers.put(headerName, request.getHeader(headerName)));
        requestInfo.put("headers", headers);
        
        input.put("user", user);
        input.put("request", requestInfo);
        
        return input;
    }
}