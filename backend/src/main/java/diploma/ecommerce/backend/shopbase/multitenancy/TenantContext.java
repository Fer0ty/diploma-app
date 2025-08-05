package diploma.ecommerce.backend.shopbase.multitenancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static Long getTenantId() {
        Long tenantId = currentTenant.get();
        log.trace("Getting TenantContext: {}", tenantId);
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        log.debug("Setting TenantContext: {}", tenantId);
        currentTenant.set(tenantId);
    }

    public static void clear() {
        log.debug("Clearing TenantContext: {}", currentTenant.get());
        currentTenant.remove();
    }
}
