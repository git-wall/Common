package org.app.common.context;

public class TenantContext {

    private static final ThreadLocal<String> TENANT = ThreadLocal.withInitial(() -> "default");

    public static void setTenant(String tenantId) {
        TENANT.set(tenantId);
    }

    public static String getTenant() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}