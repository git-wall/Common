package org.app.security.security.filter;

import org.app.common.interceptor.context.AuthContext;

public interface AuthPrincipalExtractor {
    AuthContext extract(Object principal);
}
