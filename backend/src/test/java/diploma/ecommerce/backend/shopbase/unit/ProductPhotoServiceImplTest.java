package diploma.ecommerce.backend.shopbase.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductPhoto;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductPhotoRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.impl.ProductPhotoServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductPhotoServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long PHOTO1_ID = 101L;
    private static final Long PHOTO2_ID = 102L;
    private static final Long NEW_PHOTO_ID = 111L;
    @Captor
    ArgumentCaptor<ProductPhoto> photoCaptor;
    @Mock
    private ProductPhotoRepository productPhotoRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TenantRepository tenantRepository;
    @InjectMocks
    private ProductPhotoServiceImpl productPhotoService;
    private Tenant tenant;
    private Product product;
    private ProductPhoto photo1;
    private ProductPhoto photo2;
    private ProductPhoto photoDetails;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setTenant(tenant);

        photo1 = new ProductPhoto();
        photo1.setId(PHOTO1_ID);
        photo1.setTenant(tenant);
        photo1.setProduct(product);
        photo1.setFilePath("/img1.jpg");
        photo1.setMain(true);
        photo1.setDisplayOrder(0);

        photo2 = new ProductPhoto();
        photo2.setId(PHOTO2_ID);
        photo2.setTenant(tenant);
        photo2.setProduct(product);
        photo2.setFilePath("/img2.jpg");
        photo2.setMain(false);
        photo2.setDisplayOrder(1);

        photoDetails = new ProductPhoto();
        photoDetails.setFilePath("/new_img.jpg");
        photoDetails.setDisplayOrder(2);
        photoDetails.setMain(false);


        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(true);
        when(productPhotoRepository.save(any(ProductPhoto.class))).thenAnswer(inv -> {
            ProductPhoto p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(NEW_PHOTO_ID);
            }
            return p;
        });
    }

    @Nested
    @DisplayName("getPhotosByProductId Tests")
    class GetPhotosByProductIdTests {
        @Test
        void getPhotosByProductId_ProductExists_ReturnsPhotos() {
            when(productPhotoRepository.findByTenantIdAndProductIdOrderByDisplayOrderAsc(TENANT_ID, PRODUCT_ID))
                    .thenReturn(List.of(photo1, photo2));
            List<ProductPhoto> result = productPhotoService.getPhotosByProductId(TENANT_ID, PRODUCT_ID);
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(PHOTO1_ID, result.get(0).getId());
            assertEquals(PHOTO2_ID, result.get(1).getId());
            verify(productRepository).existsByTenantIdAndId(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository).findByTenantIdAndProductIdOrderByDisplayOrderAsc(TENANT_ID, PRODUCT_ID);
        }

        @Test
        void getPhotosByProductId_ProductNotFound_ThrowsException() {
            when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(false);
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productPhotoService.getPhotosByProductId(TENANT_ID, PRODUCT_ID)
            );
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdOrderByDisplayOrderAsc(
                    anyLong(),
                    anyLong()
            );
        }
    }

    @Nested
    @DisplayName("addPhoto Tests")
    class AddPhotoTests {

        @Test
        void addPhoto_NotMainWhenPhotosExist_AddsPhoto() {
            photoDetails.setMain(false);
            when(productPhotoRepository.existsByTenantIdAndProductId(TENANT_ID, PRODUCT_ID)).thenReturn(true);
            when(productPhotoRepository.findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID)).thenReturn(
                    Optional.of(photo1));

            ProductPhoto result = productPhotoService.addPhoto(TENANT_ID, PRODUCT_ID, photoDetails);

            assertNotNull(result);
            assertEquals(NEW_PHOTO_ID, result.getId());
            assertFalse(result.isMain());
            verify(productPhotoRepository).existsByTenantIdAndProductId(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdAndMainTrue(anyLong(), anyLong());
            verify(productPhotoRepository, never()).save(photo1);
            verify(productPhotoRepository).save(photoCaptor.capture());
            ProductPhoto savedNew = photoCaptor.getValue();
            assertNotNull(savedNew);
            assertFalse(savedNew.isMain());
        }

        @Test
        void addPhoto_SetAsMain_AddsPhotoAndUnsetsOldMain() {
            photoDetails.setMain(true);
            when(productPhotoRepository.existsByTenantIdAndProductId(TENANT_ID, PRODUCT_ID)).thenReturn(true);
            when(productPhotoRepository.findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID)).thenReturn(
                    Optional.of(photo1));

            ProductPhoto result = productPhotoService.addPhoto(TENANT_ID, PRODUCT_ID, photoDetails);

            assertNotNull(result);
            assertEquals(NEW_PHOTO_ID, result.getId());
            assertTrue(result.isMain());

            verify(productPhotoRepository).findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository, times(2)).save(photoCaptor.capture());
            List<ProductPhoto> savedPhotos = photoCaptor.getAllValues();

            Optional<ProductPhoto> savedOldOpt =
                    savedPhotos.stream().filter(p -> PHOTO1_ID.equals(p.getId())).findFirst();
            assertTrue(savedOldOpt.isPresent(), "Старая главная (photo1) должна была сохраниться");
            assertFalse(savedOldOpt.get().isMain(), "У старой главной (photo1) флаг main должен быть снят");

            Optional<ProductPhoto> savedNewOpt =
                    savedPhotos.stream().filter(p -> NEW_PHOTO_ID.equals(p.getId())).findFirst();
            assertTrue(savedNewOpt.isPresent(), "Новая фотография должна была сохраниться");
            assertTrue(savedNewOpt.get().isMain(), "У новой фотографии флаг main должен быть установлен");
        }

        @Test
        void addPhoto_FirstPhoto_SetsAsMainAutomatically() {
            photoDetails.setMain(false);
            when(productPhotoRepository.existsByTenantIdAndProductId(TENANT_ID, PRODUCT_ID)).thenReturn(false);
            when(productPhotoRepository.findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID)).thenReturn(
                    Optional.empty());

            ProductPhoto result = productPhotoService.addPhoto(TENANT_ID, PRODUCT_ID, photoDetails);

            assertNotNull(result);
            assertEquals(NEW_PHOTO_ID, result.getId());
            assertTrue(result.isMain());

            verify(productPhotoRepository).existsByTenantIdAndProductId(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdAndMainTrue(anyLong(), anyLong());
            verify(productPhotoRepository).save(photoCaptor.capture());
            assertTrue(photoCaptor.getValue().isMain());
        }
    }

    @Nested
    @DisplayName("deletePhoto Tests")
    class DeletePhotoTests {
        private static ProductPhoto copyPhoto(ProductPhoto original) {
            ProductPhoto p = new ProductPhoto();
            p.setId(original.getId());
            p.setTenant(original.getTenant());
            p.setProduct(original.getProduct());
            p.setFilePath(original.getFilePath());
            p.setMain(original.isMain());
            p.setDisplayOrder(original.getDisplayOrder());
            return p;
        }

        private void mockFindSpecificPhotoByIds(Long photoId, boolean found) {
            Optional<ProductPhoto> opt;
            if (found) {
                ProductPhoto foundPhoto = (photoId.equals(PHOTO1_ID)) ? createPhoto1Copy() : createPhoto2Copy();
                opt = Optional.of(foundPhoto);
            } else {
                opt = Optional.empty();
            }
            when(productPhotoRepository.findByTenantIdAndProductIdAndId(
                    TENANT_ID,
                    PRODUCT_ID,
                    photoId
            )).thenReturn(opt);
        }

        private ProductPhoto createPhoto1Copy() { /*...*/
            return copyPhoto(photo1);
        }

        private ProductPhoto createPhoto2Copy() { /*...*/
            return copyPhoto(photo2);
        }

        @Test
        void deletePhoto_MainPhotoWithOthers_DeletesAndPromotesNext() {
            mockFindSpecificPhotoByIds(PHOTO1_ID, true);
            ProductPhoto photo2Copy = createPhoto2Copy();
            when(productPhotoRepository.findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
                    TENANT_ID,
                    PRODUCT_ID,
                    PHOTO1_ID
            ))
                    .thenReturn(List.of(photo2Copy));
            when(productPhotoRepository.save(eq(photo2Copy))).thenReturn(photo2Copy);

            productPhotoService.deletePhoto(TENANT_ID, PRODUCT_ID, PHOTO1_ID);

            verify(productPhotoRepository).findByTenantIdAndProductIdAndId(TENANT_ID, PRODUCT_ID, PHOTO1_ID);
            verify(productPhotoRepository).findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
                    TENANT_ID,
                    PRODUCT_ID,
                    PHOTO1_ID
            );
            verify(productPhotoRepository).save(photoCaptor.capture());
            assertEquals(PHOTO2_ID, photoCaptor.getValue().getId());
            assertTrue(photoCaptor.getValue().isMain());
            verify(productPhotoRepository).delete(photoCaptor.capture());
            assertEquals(PHOTO1_ID, photoCaptor.getValue().getId());
        }

        @Test
        void deletePhoto_MainPhotoNoOthers_DeletesPhoto() {
            mockFindSpecificPhotoByIds(PHOTO1_ID, true);
            when(productPhotoRepository.findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
                    TENANT_ID,
                    PRODUCT_ID,
                    PHOTO1_ID
            ))
                    .thenReturn(new ArrayList<>());

            productPhotoService.deletePhoto(TENANT_ID, PRODUCT_ID, PHOTO1_ID);

            verify(productPhotoRepository).findByTenantIdAndProductIdAndId(TENANT_ID, PRODUCT_ID, PHOTO1_ID);
            verify(productPhotoRepository).findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
                    TENANT_ID,
                    PRODUCT_ID,
                    PHOTO1_ID
            );
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
            verify(productPhotoRepository).delete(photoCaptor.capture());
            assertEquals(PHOTO1_ID, photoCaptor.getValue().getId());
        }

        @Test
        void deletePhoto_NotMainPhoto_DeletesPhoto() {
            mockFindSpecificPhotoByIds(PHOTO2_ID, true);

            productPhotoService.deletePhoto(TENANT_ID, PRODUCT_ID, PHOTO2_ID);

            verify(productPhotoRepository).findByTenantIdAndProductIdAndId(TENANT_ID, PRODUCT_ID, PHOTO2_ID);
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
                    anyLong(),
                    anyLong(),
                    anyLong()
            );
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
            verify(productPhotoRepository).delete(photoCaptor.capture());
            assertEquals(PHOTO2_ID, photoCaptor.getValue().getId());
        }


        @Test
        void deletePhoto_PhotoNotFound_ThrowsException() {
            Long nonExistentPhotoId = 999L;
            mockFindSpecificPhotoByIds(nonExistentPhotoId, false);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productPhotoService.deletePhoto(TENANT_ID, PRODUCT_ID, nonExistentPhotoId)
            );

            verify(productPhotoRepository, never()).delete(any(ProductPhoto.class));
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
        }
    }

    @Nested
    @DisplayName("setMainPhoto Tests")
    class SetMainPhotoTests {
        private static ProductPhoto copyPhoto(ProductPhoto original) {
            ProductPhoto p = new ProductPhoto();
            p.setId(original.getId());
            p.setTenant(original.getTenant());
            p.setProduct(original.getProduct());
            p.setFilePath(original.getFilePath());
            p.setMain(original.isMain());
            p.setDisplayOrder(original.getDisplayOrder());
            return p;
        }

        private ProductPhoto mockFindByIdAndTenant(Long photoId, boolean found) {
            Optional<ProductPhoto> opt;
            ProductPhoto foundPhoto = null;
            if (found) {
                foundPhoto = (photoId.equals(PHOTO1_ID)) ? createPhoto1Copy() : createPhoto2Copy();
                foundPhoto.setProduct(product);
                opt = Optional.of(foundPhoto);
            } else {
                opt = Optional.empty();
            }
            when(productPhotoRepository.findByTenantIdAndId(TENANT_ID, photoId)).thenReturn(opt);
            return foundPhoto;
        }

        private ProductPhoto createPhoto1Copy() {
            return copyPhoto(photo1);
        }

        private ProductPhoto createPhoto2Copy() {
            return copyPhoto(photo2);
        }

        @Test
        void setMainPhoto_ValidNotMain_SetsMainUnsetsOld() {
            ProductPhoto mockedPhoto2 = mockFindByIdAndTenant(PHOTO2_ID, true);
            mockedPhoto2.setMain(false);
            when(productPhotoRepository.findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID)).thenReturn(
                    Optional.of(photo1));

            ProductPhoto result = productPhotoService.setMainPhoto(TENANT_ID, PRODUCT_ID, PHOTO2_ID);

            assertNotNull(result);
            assertEquals(PHOTO2_ID, result.getId());
            assertTrue(result.isMain());
            verify(productPhotoRepository).findByTenantIdAndId(TENANT_ID, PHOTO2_ID);
            verify(productRepository).existsByTenantIdAndId(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository).findByTenantIdAndProductIdAndMainTrue(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository, times(2)).save(photoCaptor.capture());

            List<ProductPhoto> saved = photoCaptor.getAllValues();
            Optional<ProductPhoto> savedOld = saved.stream().filter(p -> PHOTO1_ID.equals(p.getId())).findFirst();
            Optional<ProductPhoto> savedNew = saved.stream().filter(p -> PHOTO2_ID.equals(p.getId())).findFirst();
            assertTrue(savedOld.isPresent());
            assertFalse(savedOld.get().isMain());
            assertTrue(savedNew.isPresent());
            assertTrue(savedNew.get().isMain());
        }

        @Test
        void setMainPhoto_AlreadyMain_NoChanges() {
            ProductPhoto mockedPhoto1 = mockFindByIdAndTenant(PHOTO1_ID, true);
            mockedPhoto1.setMain(true);

            ProductPhoto result = productPhotoService.setMainPhoto(TENANT_ID, PRODUCT_ID, PHOTO1_ID);

            assertNotNull(result);
            assertEquals(PHOTO1_ID, result.getId());
            assertTrue(result.isMain());
            verify(productPhotoRepository).findByTenantIdAndId(TENANT_ID, PHOTO1_ID);
            verify(productRepository, never()).existsByTenantIdAndId(anyLong(), anyLong());
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdAndMainTrue(anyLong(), anyLong());
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
        }

        @Test
        void setMainPhoto_PhotoNotFound_ThrowsException() {
            mockFindByIdAndTenant(PHOTO2_ID, false);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productPhotoService.setMainPhoto(TENANT_ID, PRODUCT_ID, PHOTO2_ID)
            );

            verify(productPhotoRepository).findByTenantIdAndId(TENANT_ID, PHOTO2_ID);
            verify(productRepository, never()).existsByTenantIdAndId(anyLong(), anyLong());
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
        }

        @Test
        void setMainPhoto_ProductNotFoundButPhotoExists_ThrowsException() {
            ProductPhoto mockedPhoto1 = mockFindByIdAndTenant(PHOTO1_ID, true);
            mockedPhoto1.setMain(false);
            when(productRepository.existsByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(false);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productPhotoService.setMainPhoto(TENANT_ID, PRODUCT_ID, PHOTO1_ID)
            );

            verify(productPhotoRepository).findByTenantIdAndId(TENANT_ID, PHOTO1_ID);
            verify(productRepository).existsByTenantIdAndId(TENANT_ID, PRODUCT_ID);
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
        }

        @Test
        void setMainPhoto_PhotoBelongsToAnotherProduct_ThrowsBadRequestException() {
            Product otherProduct = new Product();
            otherProduct.setId(999L);
            ProductPhoto photoFromOtherProduct = createPhoto1Copy();
            photoFromOtherProduct.setProduct(otherProduct);
            photoFromOtherProduct.setMain(false);
            when(productPhotoRepository.findByTenantIdAndId(TENANT_ID, PHOTO1_ID)).thenReturn(Optional.of(
                    photoFromOtherProduct));

            assertThrows(
                    BadRequestException.class,
                    () -> productPhotoService.setMainPhoto(TENANT_ID, PRODUCT_ID, PHOTO1_ID)
            );

            verify(productPhotoRepository).findByTenantIdAndId(TENANT_ID, PHOTO1_ID);
            verify(productRepository, never()).existsByTenantIdAndId(anyLong(), anyLong());
            verify(productPhotoRepository, never()).findByTenantIdAndProductIdAndMainTrue(anyLong(), anyLong());
            verify(productPhotoRepository, never()).save(any(ProductPhoto.class));
        }
    }
}


