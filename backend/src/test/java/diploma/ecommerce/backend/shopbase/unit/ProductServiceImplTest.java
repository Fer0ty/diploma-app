package diploma.ecommerce.backend.shopbase.unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.record.ProductSearchCriteria;
import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.OrderItemRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.impl.ProductServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PRODUCT_ID = 200L;
    @Captor
    ArgumentCaptor<Product> productCaptor;
    @Captor
    ArgumentCaptor<Specification<Product>> specCaptor;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @InjectMocks
    private ProductServiceImpl productService;
    private Tenant tenant;
    private Product product;
    private Product productDetails;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setTenant(tenant);
        product.setName("Old Name");
        product.setActive(true);
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(10);
        productDetails = new Product();
        productDetails.setName("New Name");
        productDetails.setDescription("Desc");
        productDetails.setActive(false);
        productDetails.setPrice(BigDecimal.ONE);
        productDetails.setStockQuantity(5);
        productDetails.setCategory("Cat");
    }

    @Nested
    @DisplayName("Get Product List Tests")
    class GetProductListTests {
        Pageable pageable = PageRequest.of(0, 10);

        @Test
        void getAllProducts_TenantExists_ReturnsPage() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(productRepository.findAllByTenantId(TENANT_ID, pageable)).thenReturn(new PageImpl<>(List.of(product)));
            Page<Product> result = productService.getAllProducts(TENANT_ID, pageable);
            assertEquals(1, result.getTotalElements());
            verify(productRepository).findAllByTenantId(TENANT_ID, pageable);
        }

        @Test
        void getActiveProducts_TenantExists_ReturnsPage() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(productRepository.findByTenantIdAndActive(
                    TENANT_ID,
                    true,
                    pageable
            )).thenReturn(new PageImpl<>(List.of(product)));
            Page<Product> result = productService.getActiveProducts(TENANT_ID, pageable);
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().getFirst().getActive());
            verify(productRepository).findByTenantIdAndActive(TENANT_ID, true, pageable);
        }

        @Test
        void getProductsByActiveStatus_TenantExists_ReturnsPage() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(productRepository.findByTenantIdAndActive(
                    TENANT_ID,
                    false,
                    pageable
            )).thenReturn(Page.empty(pageable));
            Page<Product> result = productService.getProductsByActiveStatus(TENANT_ID, false, pageable);
            assertTrue(result.isEmpty());
            verify(productRepository).findByTenantIdAndActive(TENANT_ID, false, pageable);
        }

        @Test
        void anyGetList_TenantNotFound_ThrowsException() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> productService.getAllProducts(TENANT_ID, pageable));
            assertThrows(ResourceNotFoundException.class, () -> productService.getActiveProducts(TENANT_ID, pageable));
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.getProductsByActiveStatus(TENANT_ID, true, pageable)
            );
            verify(productRepository, never()).findAllByTenantId(anyLong(), any());
            verify(productRepository, never()).findByTenantIdAndActive(anyLong(), anyBoolean(), any());
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests { /* ... аналогично AddressService ... */
        @Test
        void getProductById_Found_ReturnsProduct() {
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(product));
            Product result = productService.getProductById(TENANT_ID, PRODUCT_ID);
            assertNotNull(result);
            assertEquals(PRODUCT_ID, result.getId());
            verify(productRepository).findByTenantIdAndId(TENANT_ID, PRODUCT_ID);
        }

        @Test
        void getProductById_NotFound_ThrowsException() {
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(TENANT_ID, PRODUCT_ID));
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests { /* ... аналогично AddressService ... */
        @Test
        void createProduct_Valid_ReturnsCreatedProduct() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(PRODUCT_ID + 1);
                return p;
            });

            Product result = productService.createProduct(TENANT_ID, productDetails);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(tenant, result.getTenant());
            assertEquals(productDetails.getName(), result.getName());
            verify(productRepository).save(productCaptor.capture());
            assertEquals(tenant, productCaptor.getValue().getTenant());
        }

        @Test
        void createProduct_TenantNotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.createProduct(TENANT_ID, productDetails)
            );
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {
        @Test
        void updateProduct_Found_ReturnsUpdatedProduct() {
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.updateProduct(TENANT_ID, PRODUCT_ID, productDetails);

            assertNotNull(result);
            assertEquals(PRODUCT_ID, result.getId());
            assertEquals(tenant, result.getTenant());
            assertEquals(productDetails.getName(), result.getName());
            assertEquals(productDetails.getDescription(), result.getDescription());
            assertEquals(productDetails.getPrice(), result.getPrice());
            assertEquals(productDetails.getStockQuantity(), result.getStockQuantity());
            assertEquals(productDetails.getCategory(), result.getCategory());
            assertEquals(productDetails.getActive(), result.getActive());

            verify(productRepository).save(productCaptor.capture());
            assertEquals(PRODUCT_ID, productCaptor.getValue().getId());
            assertEquals(productDetails.getName(), productCaptor.getValue().getName());
        }

        @Test
        void updateProduct_ActiveIsNullInDetails_DoesNotChangeActive() {
            productDetails.setActive(null);
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.updateProduct(TENANT_ID, PRODUCT_ID, productDetails);

            assertEquals(PRODUCT_ID, result.getId());
            assertTrue(result.getActive());

            verify(productRepository).save(productCaptor.capture());
            assertTrue(productCaptor.getValue().getActive());
        }

        @Test
        void updateProduct_NotFound_ThrowsException() {
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.updateProduct(TENANT_ID, PRODUCT_ID, productDetails)
            );
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {
        @Test
        void deleteProduct_NotUsed_DeletesProduct() {
            when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(true);
            when(orderItemRepository.findByTenantIdAndProductId(
                    TENANT_ID,
                    PRODUCT_ID
            )).thenReturn(new ArrayList<>());
            doNothing().when(productRepository).deleteByTenantIdAndId(TENANT_ID, PRODUCT_ID);

            productService.deleteProduct(TENANT_ID, PRODUCT_ID);

            verify(productRepository).existsByTenantIdAndId(TENANT_ID, PRODUCT_ID);
            verify(orderItemRepository).findByTenantIdAndProductId(TENANT_ID, PRODUCT_ID);
            verify(productRepository).deleteByTenantIdAndId(TENANT_ID, PRODUCT_ID);
        }

        @Test
        void deleteProduct_IsUsed_ThrowsDataIntegrityViolationException() {
            when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(true);
            OrderItem mockItem = new OrderItem();
            mockItem.setOrder(new Order());
            mockItem.getOrder().setId(999L);
            when(orderItemRepository.findByTenantIdAndProductId(TENANT_ID, PRODUCT_ID)).thenReturn(List.of(mockItem));

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> productService.deleteProduct(TENANT_ID, PRODUCT_ID)
            );

            verify(productRepository).existsByTenantIdAndId(TENANT_ID, PRODUCT_ID);
            verify(orderItemRepository).findByTenantIdAndProductId(TENANT_ID, PRODUCT_ID);
            verify(productRepository, never()).deleteByTenantIdAndId(anyLong(), anyLong());
        }

        @Test
        void deleteProduct_NotFound_ThrowsResourceNotFoundException() {
            when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(TENANT_ID, PRODUCT_ID));
            verify(orderItemRepository, never()).findByTenantIdAndProductId(anyLong(), anyLong());
            verify(productRepository, never()).deleteByTenantIdAndId(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("findProducts Tests (Specification)")
    class FindProductsTests {
        Pageable pageable = PageRequest.of(0, 5);

        @Test
        void findProducts_AllCriteria_CallsFindAllWithSpecification() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());
            ProductSearchCriteria criteria = new ProductSearchCriteria("name", "cat", true);

            productService.findProducts(TENANT_ID, criteria, pageable);

            verify(productRepository).findAll(specCaptor.capture(), eq(pageable));
            assertNotNull(specCaptor.getValue());
        }

        @Test
        void findProducts_NoCriteria_CallsFindAllWithTenantSpecification() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());
            ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null);

            productService.findProducts(TENANT_ID, criteria, pageable);

            verify(productRepository).findAll(specCaptor.capture(), eq(pageable));
            assertNotNull(specCaptor.getValue());
        }

        @Test
        void findProducts_TenantNotFound_ThrowsException() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);
            ProductSearchCriteria criteria = new ProductSearchCriteria("n", "c", false);
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.findProducts(TENANT_ID, criteria, pageable)
            );
            verify(productRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}

