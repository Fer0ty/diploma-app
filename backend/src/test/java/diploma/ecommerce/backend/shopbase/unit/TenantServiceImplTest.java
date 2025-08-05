package diploma.ecommerce.backend.shopbase.unit;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.exception.UniquenessViolationException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.ThemeService;
import diploma.ecommerce.backend.shopbase.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TenantServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final String TENANT_NAME = "Test Store";
    private static final String TENANT_SUBDOMAIN = "teststore";
    private static final String NEW_TENANT_NAME = "Updated Store";
    private static final String NEW_TENANT_SUBDOMAIN = "updatedstore";
    private static final Long THEME_ID = 100L;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Captor
    ArgumentCaptor<Tenant> tenantCaptor;

    @Captor
    ArgumentCaptor<Theme> themeCaptor;

    private Tenant tenant;
    private Tenant tenantUpdateData;
    private Theme theme;
    private Theme themeUpdateData;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setName(TENANT_NAME);
        tenant.setSubdomain(TENANT_SUBDOMAIN);
        tenant.setCreatedAt(LocalDateTime.now().minusDays(1));
        tenant.setUpdatedAt(LocalDateTime.now().minusDays(1));

        tenantUpdateData = new Tenant();
        tenantUpdateData.setName(NEW_TENANT_NAME);
        tenantUpdateData.setSubdomain(NEW_TENANT_SUBDOMAIN);

        theme = new Theme();
        theme.setId(THEME_ID);
        theme.setPrimaryColor("#FF0000");
        theme.setSecondaryColor("#00FF00");
        theme.setFontFamily("Arial");

        themeUpdateData = new Theme();
        themeUpdateData.setPrimaryColor("#0000FF");
        themeUpdateData.setSecondaryColor("#FFFF00");
        themeUpdateData.setFontFamily("Helvetica");
    }

    @Nested
    @DisplayName("getTenantById Tests")
    class GetTenantByIdTests {

        @Test
        void getTenantById_Found_ReturnsTenant() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

            Tenant result = tenantService.getTenantById(TENANT_ID);

            assertNotNull(result);
            assertEquals(TENANT_ID, result.getId());
            assertEquals(TENANT_NAME, result.getName());
            assertEquals(TENANT_SUBDOMAIN, result.getSubdomain());

            verify(tenantRepository).findById(TENANT_ID);
        }

        @Test
        void getTenantById_NotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> tenantService.getTenantById(TENANT_ID)
            );

            verify(tenantRepository).findById(TENANT_ID);
        }
    }

    @Nested
    @DisplayName("updateTenant Tests")
    class UpdateTenantTests {

        @Test
        void updateTenant_ValidData_ReturnsUpdatedTenant() {
            LocalDateTime beforeUpdate = LocalDateTime.now();

            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.existsByName(NEW_TENANT_NAME)).thenReturn(false);
            when(tenantRepository.existsBySubdomain(NEW_TENANT_SUBDOMAIN)).thenReturn(false);
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

            Tenant result = tenantService.updateTenant(TENANT_ID, tenantUpdateData);

            assertNotNull(result);
            assertEquals(TENANT_ID, result.getId());
            assertEquals(NEW_TENANT_NAME, result.getName());
            assertEquals(NEW_TENANT_SUBDOMAIN, result.getSubdomain());
            assertTrue(result.getUpdatedAt().isAfter(beforeUpdate));

            verify(tenantRepository).findById(TENANT_ID);
            verify(tenantRepository).existsByName(NEW_TENANT_NAME);
            verify(tenantRepository).existsBySubdomain(NEW_TENANT_SUBDOMAIN);
            verify(tenantRepository).save(tenantCaptor.capture());

            Tenant savedTenant = tenantCaptor.getValue();
            assertEquals(NEW_TENANT_NAME, savedTenant.getName());
            assertEquals(NEW_TENANT_SUBDOMAIN, savedTenant.getSubdomain());
            assertTrue(savedTenant.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        void updateTenant_SameNameAndSubdomain_ReturnsUpdatedTenant() {
            tenantUpdateData.setName(TENANT_NAME); // Same name
            tenantUpdateData.setSubdomain(TENANT_SUBDOMAIN); // Same subdomain

            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

            Tenant result = tenantService.updateTenant(TENANT_ID, tenantUpdateData);

            assertNotNull(result);
            assertEquals(TENANT_NAME, result.getName());
            assertEquals(TENANT_SUBDOMAIN, result.getSubdomain());

            verify(tenantRepository).findById(TENANT_ID);
            // Should not check for existence when name/subdomain are the same
            verify(tenantRepository, never()).existsByName(anyString());
            verify(tenantRepository, never()).existsBySubdomain(anyString());
            verify(tenantRepository).save(any(Tenant.class));
        }

        @Test
        void updateTenant_DuplicateName_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.existsByName(NEW_TENANT_NAME)).thenReturn(true);

            assertThrows(
                    UniquenessViolationException.class,
                    () -> tenantService.updateTenant(TENANT_ID, tenantUpdateData)
            );

            verify(tenantRepository).findById(TENANT_ID);
            verify(tenantRepository).existsByName(NEW_TENANT_NAME);
            verify(tenantRepository, never()).save(any(Tenant.class));
        }

        @Test
        void updateTenant_DuplicateSubdomain_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.existsByName(NEW_TENANT_NAME)).thenReturn(false);
            when(tenantRepository.existsBySubdomain(NEW_TENANT_SUBDOMAIN)).thenReturn(true);

            assertThrows(
                    UniquenessViolationException.class,
                    () -> tenantService.updateTenant(TENANT_ID, tenantUpdateData)
            );

            verify(tenantRepository).findById(TENANT_ID);
            verify(tenantRepository).existsByName(NEW_TENANT_NAME);
            verify(tenantRepository).existsBySubdomain(NEW_TENANT_SUBDOMAIN);
            verify(tenantRepository, never()).save(any(Tenant.class));
        }

        @Test
        void updateTenant_NullSubdomain_SkipsSubdomainCheck() {
            tenantUpdateData.setSubdomain(null);

            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.existsByName(NEW_TENANT_NAME)).thenReturn(false);
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

            Tenant result = tenantService.updateTenant(TENANT_ID, tenantUpdateData);

            assertNotNull(result);
            assertEquals(NEW_TENANT_NAME, result.getName());

            verify(tenantRepository).findById(TENANT_ID);
            verify(tenantRepository).existsByName(NEW_TENANT_NAME);
            verify(tenantRepository, never()).existsBySubdomain(anyString());
            verify(tenantRepository).save(any(Tenant.class));
        }

        @Test
        void updateTenant_TenantNotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> tenantService.updateTenant(TENANT_ID, tenantUpdateData)
            );

            verify(tenantRepository).findById(TENANT_ID);
            verify(tenantRepository, never()).existsByName(anyString());
            verify(tenantRepository, never()).existsBySubdomain(anyString());
            verify(tenantRepository, never()).save(any(Tenant.class));
        }
    }

    @Nested
    @DisplayName("updateTenantTheme Tests")
    class UpdateTenantThemeTests {

        @Test
        void updateTenantTheme_TenantHasNoTheme_CreatesNewTheme() {
            LocalDateTime beforeUpdate = LocalDateTime.now();
            tenant.setTheme(null); // No existing theme

            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(themeService.createTheme(themeUpdateData)).thenReturn(theme);
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

            Tenant result = tenantService.updateTenantTheme(TENANT_ID, themeUpdateData);

            assertNotNull(result);
            assertEquals(TENANT_ID, result.getId());
            assertEquals(theme, result.getTheme());
            assertTrue(result.getUpdatedAt().isAfter(beforeUpdate));

            verify(tenantRepository).findById(TENANT_ID);
            verify(themeService).createTheme(themeCaptor.capture());
            verify(themeService, never()).updateTheme(anyLong(), any(Theme.class));
            verify(tenantRepository).save(tenantCaptor.capture());

            Theme capturedTheme = themeCaptor.getValue();
            assertEquals(themeUpdateData, capturedTheme);

            Tenant savedTenant = tenantCaptor.getValue();
            assertEquals(theme, savedTenant.getTheme());
            assertTrue(savedTenant.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        void updateTenantTheme_TenantHasExistingTheme_UpdatesExistingTheme() {
            LocalDateTime beforeUpdate = LocalDateTime.now();
            tenant.setTheme(theme); // Has existing theme

            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

            Tenant result = tenantService.updateTenantTheme(TENANT_ID, themeUpdateData);

            assertNotNull(result);
            assertEquals(TENANT_ID, result.getId());
            assertEquals(theme, result.getTheme()); // Same theme object
            assertTrue(result.getUpdatedAt().isAfter(beforeUpdate));

            verify(tenantRepository).findById(TENANT_ID);
            verify(themeService, never()).createTheme(any(Theme.class));
            verify(themeService).updateTheme(THEME_ID, themeUpdateData);
            verify(tenantRepository).save(tenantCaptor.capture());

            Tenant savedTenant = tenantCaptor.getValue();
            assertEquals(theme, savedTenant.getTheme());
            assertTrue(savedTenant.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        void updateTenantTheme_TenantNotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> tenantService.updateTenantTheme(TENANT_ID, themeUpdateData)
            );

            verify(tenantRepository).findById(TENANT_ID);
            verify(themeService, never()).createTheme(any(Theme.class));
            verify(themeService, never()).updateTheme(anyLong(), any(Theme.class));
            verify(tenantRepository, never()).save(any(Tenant.class));
        }
    }
}
