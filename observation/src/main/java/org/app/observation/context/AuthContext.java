package org.app.observation.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
public class AuthContext {

    // === Identity ===
    private final String subject;     // sub
    private final String username;
    private final String tenantId;

    // === Authorization ===
    private final Set<String> roles;

    // === Token tracing (safe) ===
    private final String tokenId;     // jti
    private final String tokenHash;   // sha256

    // === Extension bucket ===
    private final Map<String, Object> attributes;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
