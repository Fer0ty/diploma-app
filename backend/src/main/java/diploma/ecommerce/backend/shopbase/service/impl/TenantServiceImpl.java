package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.dto.request.TenantUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.TenantResponse;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.exception.UniquenessViolationException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.CryptoService;
import diploma.ecommerce.backend.shopbase.service.TenantService;
import diploma.ecommerce.backend.shopbase.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final ThemeService themeService;
    private final CryptoService cryptoService;

    @Override
    @Transactional(readOnly = true)
    public Tenant getTenantById(Long tenantId) {
        log.debug("Fetching tenant by ID: {}", tenantId);
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    log.warn("Tenant not found with ID: {}", tenantId);
                    return new ResourceNotFoundException("Tenant", "id", tenantId);
                });
    }

    @Override
    @Transactional
    public Tenant updateTenant(Long tenantId, Tenant tenantData) {
        log.info("Attempting to update tenant with ID: {}", tenantId);

        Tenant existingTenant = getTenantById(tenantId);

        // Проверка уникальности имени только если оно изменилось
        if (tenantData.getName() != null &&
                !existingTenant.getName().equals(tenantData.getName()) &&
                tenantRepository.existsByName(tenantData.getName())) {
            log.warn("Cannot update tenant: name '{}' already exists", tenantData.getName());
            throw new UniquenessViolationException("Tenant", "name", tenantData.getName());
        }

        // Обновляем основную информацию
        if (tenantData.getName() != null) {
            existingTenant.setName(tenantData.getName());
        }

        // Обновляем контакты
        existingTenant.setContactPhone(tenantData.getContactPhone());
        existingTenant.setContactEmail(tenantData.getContactEmail());

        // Обновляем Ozon настройки
        if (tenantData.getOzonClientId() != null) {
            existingTenant.setOzonClientId(
                    tenantData.getOzonClientId().isEmpty() ? null : cryptoService.encrypt(tenantData.getOzonClientId())
            );
        }

        if (tenantData.getOzonWarehouseId() != null) {
            existingTenant.setOzonWarehouseId(tenantData.getOzonWarehouseId());
        }

        if (tenantData.isOzonSyncEnabled() != existingTenant.isOzonSyncEnabled()) {
            existingTenant.setOzonSyncEnabled(tenantData.isOzonSyncEnabled());
        }

        // Шифруем и сохраняем API ключи
        if (tenantData.getOzonApiKey() != null) {
            existingTenant.setOzonApiKey(
                    tenantData.getOzonApiKey().isEmpty() ? null : cryptoService.encrypt(tenantData.getOzonApiKey())
            );
        }

        if (tenantData.getWildberriesApiKey() != null) {
            existingTenant.setWildberriesApiKey(
                    tenantData.getWildberriesApiKey().isEmpty() ? null : cryptoService.encrypt(tenantData.getWildberriesApiKey())
            );
        }

        if (tenantData.getYookassaIdempotencyKey() != null) {
            existingTenant.setYookassaIdempotencyKey(
                    tenantData.getYookassaIdempotencyKey().isEmpty() ? null : cryptoService.encrypt(tenantData.getYookassaIdempotencyKey())
            );
        }

        if (tenantData.getYookassaSecretKey() != null) {
            existingTenant.setYookassaSecretKey(
                    tenantData.getYookassaSecretKey().isEmpty() ? null : cryptoService.encrypt(tenantData.getYookassaSecretKey())
            );
        }

        existingTenant.setUpdatedAt(LocalDateTime.now());

        Tenant updatedTenant = tenantRepository.save(existingTenant);
        log.info("Tenant with ID {} updated successfully", tenantId);

        return updatedTenant;
    }

    @Override
    @Transactional
    public Tenant updateTenantFromRequest(Long tenantId, TenantUpdateRequest request) {
        log.info("Updating tenant {} from request", tenantId);

        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setOzonApiKey(request.getOzonApiKey());
        tenant.setWildberriesApiKey(request.getWildberriesApiKey());
        tenant.setYookassaIdempotencyKey(request.getYookassaIdempotencyKey());
        tenant.setYookassaSecretKey(request.getYookassaSecretKey());

        return updateTenant(tenantId, tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantResponse(Long tenantId) {
        Tenant tenant = getTenantById(tenantId);
        return mapToResponse(tenant);
    }

    @Override
    @Transactional
    public Tenant updateTenantTheme(Long tenantId, Theme themeData) {
        log.info("Attempting to update theme for tenant with ID: {}", tenantId);

        Tenant existingTenant = getTenantById(tenantId);

        if (existingTenant.getTheme() == null) {
            Theme newTheme = themeService.createTheme(themeData);
            existingTenant.setTheme(newTheme);
        } else {
            Theme existingTheme = existingTenant.getTheme();
            themeService.updateTheme(existingTheme.getId(), themeData);
        }

        existingTenant.setUpdatedAt(LocalDateTime.now());

        Tenant updatedTenant = tenantRepository.save(existingTenant);
        log.info("Theme updated successfully for tenant with ID: {}", tenantId);

        return updatedTenant;
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .contactPhone(tenant.getContactPhone())
                .contactEmail(tenant.getContactEmail())
                // Расшифровываем API ключи при отдаче
                .ozonApiKey(tenant.getOzonApiKey() != null ? cryptoService.decrypt(tenant.getOzonApiKey()) : null)
                .wildberriesApiKey(tenant.getWildberriesApiKey() != null ? cryptoService.decrypt(tenant.getWildberriesApiKey()) : null)
                .yookassaIdempotencyKey(tenant.getYookassaIdempotencyKey() != null ? cryptoService.decrypt(tenant.getYookassaIdempotencyKey()) : null)
                .yookassaSecretKey(tenant.getYookassaSecretKey() != null ? cryptoService.decrypt(tenant.getYookassaSecretKey()) : null)
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .active(tenant.isActive())
                .build();
    }
}