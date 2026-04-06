package org.app.security.filter;

import org.app.observation.context.AuthContext;
import org.app.observation.context.ContextHolder;
import org.app.security.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            AuthContext ctx = buildAuthContext(auth, req);
            ContextHolder.setAuth(ctx);

            chain.doFilter(req, res);
        } finally {
            ContextHolder.clearAuth();
        }
    }

    private AuthContext buildAuthContext(Authentication auth, HttpServletRequest req) {
        return AuthContext.builder()
            .subject(AuthUtils.extractSub(auth))
            .username(AuthUtils.extractUsername(auth))
            .tenantId(AuthUtils.resolveTenantId(auth, req))
            .roles(AuthUtils.extractRoles(auth))
            .tokenId(AuthUtils.extractTokenId(auth))
            .tokenHash(AuthUtils.extractTokenHash(req))
            .attributes(AuthUtils.extractAttributes(auth))
            .build();
    }
}
