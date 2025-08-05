package diploma.ecommerce.backend.shopbase.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantUserRepository;
import diploma.ecommerce.backend.shopbase.repository.ThemeRepository;
import diploma.ecommerce.backend.shopbase.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    protected static final String rootDomain = ".diploma.ru";
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TenantRepository tenantRepository;
    @Autowired
    protected TenantUserRepository tenantUserRepository;
    @Autowired
    protected ThemeRepository themeRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected JwtUtil jwtUtil;
    protected Tenant tenant1;
    protected Tenant tenant2;
    protected TenantUser userTenant1;
    protected TenantUser userTenant2;
    protected String jwtTenant1;
    protected String jwtTenant2;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    protected static RequestPostProcessor serverName(String serverName) {
        return request -> {
            request.setServerName(serverName);
            return request;
        };
    }

    @BeforeEach
    void setUpTenantsAndUsers() {
        tenant1 = new Tenant();
        tenant1.setName("Test Store 1");
        tenant1.setSubdomain("store1");
        tenant1.setActive(true);
        tenant1 = tenantRepository.saveAndFlush(tenant1);

        tenant2 = new Tenant();
        tenant2.setName("Test Store 2");
        tenant2.setSubdomain("store2");
        tenant2.setActive(true);
        tenant2 = tenantRepository.saveAndFlush(tenant2);

        userTenant1 = new TenantUser();
        userTenant1.setTenant(tenant1);
        userTenant1.setUsernameInTenant("editor1");
        userTenant1.setFirstName("EditorFirstName1");
        userTenant1.setLastName("EditorLastName1");
        userTenant1.setEmail("editor1@store1.com");
        userTenant1.setPasswordHash(passwordEncoder.encode("password"));
        userTenant1.setRole("ROLE_ADMIN");
        userTenant1.setActive(true);
        userTenant1 = tenantUserRepository.saveAndFlush(userTenant1);

        userTenant2 = new TenantUser();
        userTenant2.setTenant(tenant2);
        userTenant2.setUsernameInTenant("editor2");
        userTenant1.setFirstName("EditorFirstName2");
        userTenant1.setLastName("EditorLastName2");
        userTenant2.setEmail("editor2@store2.com");
        userTenant2.setPasswordHash(passwordEncoder.encode("password"));
        userTenant2.setRole("ROLE_ADMIN");
        userTenant2.setActive(true);
        userTenant2 = tenantUserRepository.saveAndFlush(userTenant2);

        jwtTenant1 = jwtUtil.generateToken(userTenant1);
        jwtTenant2 = jwtUtil.generateToken(userTenant2);
    }

    protected HttpHeaders getAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        return headers;
    }
}
