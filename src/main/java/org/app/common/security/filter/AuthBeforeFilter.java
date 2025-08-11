package org.app.common.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.common.exception.SecurityFilterException;
import org.app.common.security.provider.JwtProvider;
import org.app.common.utils.RequestUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthBeforeFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            RequestUtils.getTokenBy(request)
                    .flatMap(jwtProvider::validJwtToken$getClaims)
                    .ifPresent(payload -> {
                        String username = payload.getSubject();
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });

        } catch (SecurityFilterException ex) {
            log.info("Cannot set user authentication: {}", ex.getMessage());
        } catch (Exception e) {
            log.info("Cannot set user authentication with error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
