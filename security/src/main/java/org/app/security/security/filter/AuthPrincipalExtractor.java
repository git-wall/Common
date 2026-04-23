package org.app.security.security.filter;

import org.app.observation.context.AuthContext;

public interface AuthPrincipalExtractor {
    AuthContext extract(Object principal);
}
