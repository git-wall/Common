package org.app.common.opa;

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

public class OpaAuthorizationFilter extends OncePerRequestFilter {

    private final OpaClient opaClient;
    private final OpaInputBuilder inputBuilder;


    public OpaAuthorizationFilter(OpaClient opaClient, OpaInputBuilder inputBuilder) {
        this.opaClient = opaClient;
        this.inputBuilder = inputBuilder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> input = inputBuilder.buildInput(request, authentication);
            Map<String, Object> body = new HashMap<>(1);
            body.put("input", input);

            boolean allowed = opaClient.checkPermission(body);

            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied by policy");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
