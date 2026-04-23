package org.app.security.permission;

import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final Enforcer enforcer;

    public boolean isAllow(Map<String, Object> data, Object resource, String action) {
        return enforcer.enforce(data, resource, action);
    }

    public Optional<Boolean> isAccept(Map<String, Object> data, Object resource, String action) {
        return Optional.of(enforcer.enforce(data, resource, action));
    }
}
