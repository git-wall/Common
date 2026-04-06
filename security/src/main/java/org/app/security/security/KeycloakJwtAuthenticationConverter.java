package org.app.security.security;

import org.app.jackson.JType;
import org.app.jackson.JacksonUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Realm roles

        Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            for (String r: realmAccess.get("roles")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()));
            }
        }

        // 2. Client roles (resource_access)
        Map<String, Map<String, Object>> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(client -> {
                List<String> roles = JacksonUtils.convert(client.get("roles"), JType.LIST_STR);
                for (String r: roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()));
                }
            });
        }

        // 3. OAuth scopes
        String scope = jwt.getClaimAsString("scope");
        if (scope != null) {
            Arrays.stream(scope.split(" "))
                .map(s -> "SCOPE_" + s)
                .forEach(a -> authorities.add(new SimpleGrantedAuthority(a)));
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
