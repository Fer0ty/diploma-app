package diploma.ecommerce.backend.shopbase.scheduler;

import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.OzonSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ozon.sync.enabled", havingValue = "true", matchIfMissing = true)
public class OzonSyncScheduler {

    private final OzonSyncService ozonSyncService;
    private final TenantRepository tenantRepository;

    @Scheduled(fixedDelayString = "${ozon.sync.interval:3600000}") // каждый час по умолчанию
    public void syncAllTenants() {
        log.info("Starting scheduled Ozon sync for all tenants");

        List<Tenant> tenantsWithOzon = tenantRepository.findAll().stream()
                .filter(t -> t.isOzonSyncEnabled() && t.getOzonApiKey() != null && t.getOzonClientId() != null)
                .toList();

        log.info("Found {} tenants with Ozon sync enabled", tenantsWithOzon.size());

        for (Tenant tenant : tenantsWithOzon) {
            try {
                TenantContext.setTenantId(tenant.getId());
                log.info("Syncing tenant {} ({})", tenant.getId(), tenant.getName());
                ozonSyncService.syncProductsForTenant(tenant.getId());
            } catch (Exception e) {
                log.error("Error syncing tenant {}: ", tenant.getId(), e);
            } finally {
                TenantContext.clear();
            }
        }

        log.info("Completed scheduled Ozon sync");
    }
}