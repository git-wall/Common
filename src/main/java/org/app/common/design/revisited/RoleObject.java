package org.app.common.design.revisited;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RoleObject<T> {
    @Getter
    private final T core;
    private final Map<Class<?>, Object> roles = new HashMap<>(4);

    public RoleObject(T core) {
        this.core = core;
    }

    public <R> void addRole(Class<R> roleType, R role) {
        roles.put(roleType, role);
    }

    public <R> void addRole(Class<R> roleType, Function<T, R> roleFactory) {
        roles.put(roleType, roleFactory.apply(core));
    }

    @SuppressWarnings("unchecked")
    public <R> Optional<R> getRole(Class<R> roleType) {
        return Optional.ofNullable((R) roles.get(roleType));
    }

}