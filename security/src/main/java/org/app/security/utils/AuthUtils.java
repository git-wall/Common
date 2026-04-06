package org.app.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.app.web.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtils {

    public static String resolveTenantId(Authentication auth, HttpServletRequest req) {
        // 1. Header wins
        String tenant = RequestUtils.getTenantId(req);
        if (StringUtils.hasText(tenant)) {
            return tenant;
        }

        // 2. Token claim
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt) {
            tenant = ((Jwt) principal).getClaimAsString("tenant");
            if (StringUtils.hasText(tenant)) {
                return tenant;
            }
        }

        // 3. Unknown / public
        return null;
    }

    public static String extractSub(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt) {
            return ((Jwt) principal).getSubject();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return auth.getName();
    }

    public static String extractUsername(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt) {
            return ((Jwt) principal).getClaimAsString("preferred_username");
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return auth.getName();
    }

    public static String extractClientId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (auth.getPrincipal() instanceof Jwt) {
            return ((Jwt) principal).getClaimAsString("azp"); // authorized party
        }
        return null;
    }

    public static Set<String> extractRoles(Authentication auth) {
        return auth.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toUnmodifiableSet());
    }

    public static String extractTokenId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (auth.getPrincipal() instanceof Jwt) {
            return ((Jwt) principal).getId(); // jti
        }
        return null;
    }

    public static String extractTokenHash(HttpServletRequest req) {
        String token = RequestUtils.getToken(req);
        if (!StringUtils.hasText(token)) return null;

        return DigestUtils.sha256Hex(token);
    }

    public static Map<String, Object> extractAttributes(Authentication auth) {
        Map<String, Object> attrs = new HashMap<>();
        Object principal = auth.getPrincipal();
        if (auth.getPrincipal() instanceof Jwt) {
            ((Jwt) principal).getClaims().forEach((k, v) -> {
                if (!isSensitive(k)) {
                    attrs.put(k, v);
                }
            });
        }

        return Map.copyOf(attrs);
    }

    public static boolean isSensitive(String claim) {
        return Set.of("exp", "iat", "nbf", "aud").contains(claim);
    }
}
