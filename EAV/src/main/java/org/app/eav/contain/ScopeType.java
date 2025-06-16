package org.app.eav.contain;

public enum ScopeType {
    /**
     * Can be show anywhere
     */
    PUBLIC,
    /**
     * Can see in api, but not show in app for client
     */
    PROTECTED,
    /**
     * Use in app only, not visible in network or api
     */
    PRIVATE
}
