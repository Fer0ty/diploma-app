package diploma.ecommerce.backend.shopbase.integration;

import java.math.BigDecimal;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.ProductCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.ProductUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest extends BaseIntegrationTest {

    private static final String PRODUCTS_URL = "/api/v1/products";


    @Autowired
    private ProductRepository productRepository;

    private Product product1Tenant1;
    private Product product2Tenant1;
    private Product product1Tenant2;

    private static Product createProductEntity(String name, Tenant tenant, BigDecimal price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setTenant(tenant);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setDescription("Test desc for " + name);
        p.setCategory("Test Category");
        p.setActive(true);
        return p;
    }

    @BeforeEach
    void setUpProducts() {
        productRepository.deleteAllInBatch();

        product1Tenant1 = createProductEntity("Laptop Pro", tenant1, new BigDecimal("1200.00"), 10);
        product1Tenant1 = productRepository.saveAndFlush(product1Tenant1);

        product2Tenant1 = createProductEntity("Mouse Wireless", tenant1, new BigDecimal("25.00"), 50);
        product2Tenant1 = productRepository.saveAndFlush(product2Tenant1);

        product1Tenant2 = createProductEntity("Keyboard Mechanical", tenant2, new BigDecimal("150.00"), 20);
        product1Tenant2 = productRepository.saveAndFlush(product1Tenant2);
    }

    // GET Tests
    @Test
    @Order(1)
    @DisplayName("GET /products - Tenant 1 (via Subdomain) - Should return only Tenant 1's products")
    void getProducts_forTenant1_viaSubdomain_returnsOnlyTenant1Products() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Laptop Pro", "Mouse Wireless")))
                .andExpect(jsonPath("$.content[?(@.name == 'Keyboard Mechanical')]", empty()));
    }

    @Test
    @Order(2)
    @DisplayName("GET /products - Tenant 2 (via Subdomain) - Should return only Tenant 2's products")
    void getProducts_forTenant2_viaSubdomain_returnsOnlyTenant2Products() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL)
                                .with(serverName(tenant2.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Keyboard Mechanical")));
    }

    @Test
    @Order(3)
    @DisplayName("GET /products/{id} - Tenant 1 (via Subdomain) - Should return product if belongs to Tenant 1")
    void getProductById_forTenant1_viaSubdomain_whenProductBelongsToTenant1_returnsProduct() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/{productId}", product1Tenant1.getId())
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(product1Tenant1.getId().intValue())))
                .andExpect(jsonPath("$.name", is(product1Tenant1.getName())));
    }

    @Test
    @Order(4)
    @DisplayName("GET /products/{id} - Tenant 1 (via Subdomain) - Should return 404 if product belongs to Tenant 2")
    void getProductById_forTenant1_viaSubdomain_whenProductBelongsToTenant2_returnsNotFound() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/{productId}", product1Tenant2.getId())
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /products/{id} - (via Subdomain) - Should return 404 if product does not exist")
    void getProductById_viaSubdomain_whenProductDoesNotExist_returnsNotFound() throws Exception {
        long nonExistentId = 9999L;
        mockMvc.perform(get(PRODUCTS_URL + "/{productId}", nonExistentId)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // test adminki

    @Test
    @Order(5)
    @DisplayName("POST /products - Tenant 1 (Admin via JWT) - Should create product and assign to Tenant 1")
    void createProduct_forTenant1Admin_viaJwt_createsAndAssignsProduct() throws Exception {
        ProductCreateRequest createRequest = new ProductCreateRequest();
        createRequest.setName("New Gadget T1");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setStockQuantity(5);
        createRequest.setCategory("Gadgets");
        createRequest.setActive(true);

        mockMvc.perform(post(PRODUCTS_URL)
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("New Gadget T1")))
                .andDo(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    Product createdProductResponse = objectMapper.readValue(
                            responseString,
                            Product.class
                    );
                    long createdProductId = createdProductResponse.getId();

                    Optional<Product> savedProductOpt = productRepository.findById(createdProductId);
                    assertTrue(savedProductOpt.isPresent(), "Created product not found in DB");
                    Product savedProduct = savedProductOpt.get();
                    assertNotNull(savedProduct.getTenant(), "Tenant not set on saved product");
                    assertEquals(tenant1.getId(), savedProduct.getTenant().getId(), "Product assigned to wrong tenant");
                });
    }


    @Test
    @Order(6)
    @DisplayName("PUT /products/{id} - Tenant 1 (Admin via JWT) - Should update own product")
    void updateProduct_forTenant1Admin_viaJwt_whenOwnProduct_updatesProduct() throws Exception {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated Laptop Pro via JWT");
        updateRequest.setPrice(new BigDecimal("1150.00"));

        mockMvc.perform(put(PRODUCTS_URL + "/{productId}", product1Tenant1.getId())
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Updated Laptop Pro via JWT")))
                .andExpect(jsonPath("$.price", is(1150.00)));

        Optional<Product> updatedProductOpt = productRepository.findById(product1Tenant1.getId());
        assertTrue(updatedProductOpt.isPresent());
        assertEquals("Updated Laptop Pro via JWT", updatedProductOpt.get().getName());
        assertEquals(0, new BigDecimal("1150.00").compareTo(updatedProductOpt.get().getPrice()));
    }

    @Test
    @Order(7)
    @DisplayName("PUT /products/{id} - Tenant 1 Admin (via JWT) - Should return 404 for another tenant's product")
    void updateProduct_forTenant1Admin_viaJwt_whenAnothersProduct_returnsNotFound() throws Exception {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Attempted Update on T2 product");

        mockMvc.perform(put(PRODUCTS_URL + "/{productId}", product1Tenant2.getId())
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }


    @Test
    @Order(8)
    @DisplayName("DELETE /products/{id} - Tenant 1 (Admin via JWT) - Should delete own product")
    void deleteProduct_forTenant1Admin_viaJwt_whenOwnProduct_deletesProduct() throws Exception {
        Long idToDelete = product2Tenant1.getId();

        mockMvc.perform(delete(PRODUCTS_URL + "/{productId}", idToDelete)
                                .headers(getAuthHeaders(jwtTenant1)))
                .andExpect(status().isNoContent());

        assertFalse(productRepository.existsById(idToDelete));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /products/{id} - Tenant 1 Admin (via JWT) - Should return 404 for another tenant's product")
    void deleteProduct_forTenant1Admin_viaJwt_whenAnothersProduct_returnsNotFound() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/{productId}", product1Tenant2.getId())
                                .headers(getAuthHeaders(jwtTenant1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /products - Invalid data - Should return 400 Bad Request")
    void createProduct_withInvalidData_shouldReturnBadRequest() throws Exception {
        ProductCreateRequest createRequest = new ProductCreateRequest();
        createRequest.setName("");
        createRequest.setPrice(new BigDecimal("-10.00"));
        createRequest.setStockQuantity(-5);

        mockMvc.perform(post(PRODUCTS_URL)
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.name", containsString("Product name cannot be blank")))
                .andExpect(jsonPath("$.fieldErrors.price", containsString("Price must be positive")))
                .andExpect(jsonPath(
                        "$.fieldErrors.stockQuantity",
                        containsString("Stock quantity cannot be negative")
                ));
    }
}
