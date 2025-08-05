package diploma.ecommerce.backend.shopbase.integration;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.AddressCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.AddressUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AddressController Integration Tests")
class AddressControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ADDRESSES_URL = "/api/v1/addresses";

    @Autowired
    private AddressRepository addressRepository;

    private Address address1Tenant1;
    private Address address2Tenant1;
    private Address address1Tenant2;

    private static AddressCreateRequest createAddressRequest(String country, String city, String street, String house) {
        AddressCreateRequest request = new AddressCreateRequest();
        request.setCountry(country);
        request.setCity(city);
        request.setStreet(street);
        request.setHouseNumber(house);
        request.setPostalCode("123456");
        return request;
    }

    private static Address createAddressEntity(
            String country,
            String city,
            String street,
            String house,
            Tenant tenant
    ) {
        Address address = new Address();
        address.setCountry(country);
        address.setCity(city);
        address.setStreet(street);
        address.setHouseNumber(house);
        address.setPostalCode("000000");
        address.setTenant(tenant);
        return address;
    }

    @BeforeEach
    void setUpAddresses() {
        address1Tenant1 = createAddressEntity("Russia", "Moscow", "Tverskaya", "1", tenant1);
        addressRepository.saveAndFlush(address1Tenant1);

        address2Tenant1 = createAddressEntity("Russia", "St. Petersburg", "Nevsky", "10", tenant1);
        addressRepository.saveAndFlush(address2Tenant1);

        address1Tenant2 = createAddressEntity("USA", "New York", "5th Avenue", "100", tenant2);
        addressRepository.saveAndFlush(address1Tenant2);
    }

    //  GET Tests
    @Test
    @Order(1)
    @DisplayName("GET /addresses - Tenant 1 (via Subdomain) - Should return only Tenant 1's addresses")
    void getAllAddresses_forTenant1_viaSubdomain_returnsOnlyTenant1Addresses() throws Exception {
        mockMvc.perform(get(ADDRESSES_URL)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].street", containsInAnyOrder("Tverskaya", "Nevsky")))
                .andExpect(jsonPath("$[?(@.street == '5th Avenue')]", empty()));
    }

    @Test
    @Order(2)
    @DisplayName("GET /addresses - Tenant 2 (via Subdomain) - Should return only Tenant 2's addresses")
    void getAllAddresses_forTenant2_viaSubdomain_returnsOnlyTenant2Addresses() throws Exception {
        mockMvc.perform(get(ADDRESSES_URL)
                                .with(serverName(tenant2.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].street", is("5th Avenue")));
    }

    @Test
    @Order(3)
    @DisplayName("GET /addresses/{id} - Tenant 1 (via Subdomain) - Should return address if belongs to Tenant 1")
    void getAddressById_forTenant1_viaSubdomain_whenAddressBelongsToTenant1_returnsAddress() throws Exception {
        mockMvc.perform(get(ADDRESSES_URL + "/{id}", address1Tenant1.getId())
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(address1Tenant1.getId().intValue())))
                .andExpect(jsonPath("$.street", is(address1Tenant1.getStreet())));
    }

    @Test
    @Order(4)
    @DisplayName("GET /addresses/{id} - Tenant 1 (via Subdomain) - Should return 404 if address belongs to Tenant 2")
    void getAddressById_forTenant1_viaSubdomain_whenAddressBelongsToTenant2_returnsNotFound() throws Exception {
        mockMvc.perform(get(ADDRESSES_URL + "/{id}", address1Tenant2.getId())
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("GET /addresses/{id} - (via Subdomain) - Should return 404 if address does not exist")
    void getAddressById_viaSubdomain_whenAddressDoesNotExist_returnsNotFound() throws Exception {
        long nonExistentId = 9999L;
        mockMvc.perform(get(ADDRESSES_URL + "/{id}", nonExistentId)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //  POST Tests
    @Test
    @Order(6)
    @DisplayName("POST /addresses - Tenant 1 (via Subdomain) - Should create address and assign to Tenant 1")
    void createAddress_forTenant1_viaSubdomain_createsAndAssignsAddress() throws Exception {
        AddressCreateRequest request = createAddressRequest("UK", "London", "Baker Street", "221B");

        mockMvc.perform(post(ADDRESSES_URL)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.street", is("Baker Street")))
                .andExpect(jsonPath("$.country", is("UK")))
                .andDo(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    long createdId = objectMapper.readTree(responseString).get("id").asLong();
                    Optional<Address> savedAddressOpt = addressRepository.findByTenantIdAndId(
                            tenant1.getId(),
                            createdId
                    );
                    assertTrue(savedAddressOpt.isPresent(), "Created address not found in DB for tenant1");
                    Address savedAddress = savedAddressOpt.get();
                    assertNotNull(savedAddress.getTenant(), "Tenant not set on saved address");
                    assertEquals(tenant1.getId(), savedAddress.getTenant().getId(), "Address assigned to wrong tenant");
                });
    }


    @Test
    @Order(7)
    @DisplayName("POST /addresses - Invalid data (via Subdomain) - Should return 400 Bad Request")
    void createAddress_withInvalidData_viaSubdomain_returnsBadRequest() throws Exception {
        AddressCreateRequest request = new AddressCreateRequest();
        request.setStreet("Way too long street name".repeat(20));
        mockMvc.perform(post(ADDRESSES_URL)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.country", is("Country cannot be blank")))
                .andExpect(jsonPath("$.fieldErrors.city", is("City cannot be blank")))
                .andExpect(jsonPath("$.fieldErrors.street", containsString("Street must be less than 255 characters")))
                .andExpect(jsonPath("$.fieldErrors.houseNumber", is("House number cannot be blank")));
    }


    // PUT Tests
    @Test
    @Order(8)
    @DisplayName("PUT /addresses/{id} - Tenant 1 (Admin via JWT) - Should update own address")
    void updateAddress_forTenant1Admin_viaJwt_whenOwnAddress_updatesSuccessfully() throws Exception {
        AddressUpdateRequest request = new AddressUpdateRequest();
        request.setCity("Novosibirsk");
        request.setComment("Updated via JWT");

        mockMvc.perform(put(ADDRESSES_URL + "/{id}", address1Tenant1.getId())
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(address1Tenant1.getId().intValue())))
                .andExpect(jsonPath("$.city", is("Novosibirsk")))
                .andExpect(jsonPath("$.comment", is("Updated via JWT")));

        Optional<Address> updatedAddressOpt = addressRepository.findByTenantIdAndId(
                tenant1.getId(),
                address1Tenant1.getId()
        );
        assertTrue(updatedAddressOpt.isPresent());
        assertEquals("Novosibirsk", updatedAddressOpt.get().getCity());
        assertEquals("Updated via JWT", updatedAddressOpt.get().getComment());
    }

    @Test
    @Order(9)
    @DisplayName("PUT /addresses/{id} - Tenant 1 Admin (via JWT) - Should return 404 for another tenant's address")
    void updateAddress_forTenant1Admin_viaJwt_whenAnothersAddress_returnsNotFound() throws Exception {
        AddressUpdateRequest request = new AddressUpdateRequest();
        request.setCity("Some City");

        mockMvc.perform(put(ADDRESSES_URL + "/{id}", address1Tenant2.getId())
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("PUT /addresses/{id} - Anonymous - Should return 401 Unauthorized")
    void updateAddress_forAnonymous_returnsUnauthorized() throws Exception {
        AddressUpdateRequest request = new AddressUpdateRequest();
        request.setCity("Forbidden City");

        mockMvc.perform(put(ADDRESSES_URL + "/{id}", address1Tenant1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    //  DELETE Tests
    @Test
    @Order(11)
    @DisplayName("DELETE /addresses/{id} - Tenant 1 (Admin via JWT) - Should delete own address")
    void deleteAddress_forTenant1Admin_viaJwt_whenOwnAddress_deletesSuccessfully() throws Exception {
        Long idToDelete = address2Tenant1.getId();

        mockMvc.perform(delete(ADDRESSES_URL + "/{id}", idToDelete)
                                .headers(getAuthHeaders(jwtTenant1)))
                .andExpect(status().isNoContent());

        assertFalse(addressRepository.findByTenantIdAndId(tenant1.getId(), idToDelete).isPresent());
    }

    @Test
    @Order(12)
    @DisplayName("DELETE /addresses/{id} - Tenant 1 Admin (via JWT) - Should return 404 for another tenant's address")
    void deleteAddress_forTenant1Admin_viaJwt_whenAnothersAddress_returnsNotFound() throws Exception {
        mockMvc.perform(delete(ADDRESSES_URL + "/{id}", address1Tenant2.getId())
                                .headers(getAuthHeaders(jwtTenant1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /addresses/{id} - Anonymous - Should return 401 Unauthorized")
    void deleteAddress_forAnonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(ADDRESSES_URL + "/{id}", address1Tenant1.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
