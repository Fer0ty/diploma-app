package diploma.ecommerce.backend.shopbase.scheduler;

import java.util.List;

import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.UnifiedMarketplaceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "marketplace.sync.enabled", havingValue = "true", matchIfMissing = true)
public class UnifiedMarketplaceSyncScheduler {

    private final UnifiedMarketplaceSyncService syncService;
    private final TenantRepository tenantRepository;

    @Scheduled(fixedDelayString = "${marketplace.sync.interval:3600000}")
    public void syncAllMarketplaces() {
        log.info("Starting scheduled marketplace sync for all tenants");

        List<Tenant> tenantsWithMarketplaces = tenantRepository.findAll().stream()
                .filter(t -> t.isOzonSyncEnabled() || t.isWildberriesSyncEnabled())
                .toList();

        log.info("Found {} tenants with marketplace sync enabled", tenantsWithMarketplaces.size());

        for (Tenant tenant : tenantsWithMarketplaces) {
            try {
                TenantContext.setTenantId(tenant.getId());
                log.info("Syncing marketplaces for tenant {} ({})", tenant.getId(), tenant.getName());
                syncService.syncAllMarketplaces(tenant.getId());
            } catch (Exception e) {
                log.error("Error syncing marketplaces for tenant {}: ", tenant.getId(), e);
            } finally {
                TenantContext.clear();
            }
        }

        log.info("Completed scheduled marketplace sync");
    }
}