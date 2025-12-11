package org.app.common.opa;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface OpaInputBuilder {
    Map<String, Object> buildInput(HttpServletRequest request, Authentication authentication);
}

