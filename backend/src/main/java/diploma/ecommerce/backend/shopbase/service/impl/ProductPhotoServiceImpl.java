package diploma.ecommerce.backend.shopbase.service.impl;

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
import diploma.ecommerce.backend.shopbase.service.ProductPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductPhoto> getPhotosByProductId(Long tenantId, Long productId) {
        if (!productRepository.existsByTenantIdAndId(tenantId, productId)) {
            throw new ResourceNotFoundException("Product", "id", productId, tenantId);
        }
        return productPhotoRepository.findByTenantIdAndProductIdOrderByDisplayOrderAsc(tenantId, productId);
    }

    @Override
    @Transactional
    public ProductPhoto addPhoto(Long tenantId, Long productId, ProductPhoto photoRequest) {
        log.info("Adding photo for product {} (tenant {})", productId, tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId, tenantId));

        ProductPhoto newPhoto = new ProductPhoto();
        newPhoto.setTenant(tenant);
        newPhoto.setProduct(product);
        newPhoto.setFilePath(photoRequest.getFilePath());
        newPhoto.setDisplayOrder(photoRequest.getDisplayOrder() != null ? photoRequest.getDisplayOrder() : 0);

        if (photoRequest.isMain()) {
            unsetCurrentMainPhoto(tenantId, productId);
            newPhoto.setMain(true);
            log.debug("Setting new photo as main for product {}", productId);
        } else {
            newPhoto.setMain(false);
            if (!productPhotoRepository.existsByTenantIdAndProductId(tenantId, productId)) {
                log.info("Setting the first added photo as main for product {}", productId);
                newPhoto.setMain(true);
            }
        }

        newPhoto.setId(null);

        ProductPhoto savedPhoto = productPhotoRepository.save(newPhoto);
        log.info("Photo {} added successfully for product {} (tenant {})", savedPhoto.getId(), productId, tenantId);
        return savedPhoto;
    }

    @Override
    @Transactional
    public void deletePhoto(Long tenantId, Long productId, Long photoId) {
        log.warn("Attempting to delete photo {} for product {} (tenant {})", photoId, productId, tenantId);

        ProductPhoto photoToDelete = productPhotoRepository
                .findByTenantIdAndProductIdAndId(tenantId, productId, photoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductPhoto",
                        "composite ID (productId, photoId)",
                        String.format("productId=%d, photoId=%d", productId, photoId),
                        tenantId
                ));

        if (photoToDelete.isMain()) {
            log.warn(
                    "Deleting the main photo ({}) for product {} (tenant {})",
                    photoId,
                    productId,
                    tenantId
            );

            List<ProductPhoto> remainingPhotos = productPhotoRepository
                    .findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(tenantId, productId, photoId);

            if (!remainingPhotos.isEmpty()) {
                ProductPhoto newMainPhoto = remainingPhotos.getFirst();
                newMainPhoto.setMain(true);
                productPhotoRepository.save(newMainPhoto);
                log.info(
                        "Promoted photo {} to main for product {} (tenant {}) after deleting the previous main photo.",
                        newMainPhoto.getId(), productId, tenantId
                );
            } else {
                log.warn(
                        "Deleted the only photo for product {} (tenant {}). No photo set as main.",
                        productId,
                        tenantId
                );
            }
        }

        productPhotoRepository.delete(photoToDelete);
        log.info("Photo {} deleted successfully for product {} (tenant {})", photoId, productId, tenantId);
    }

    @Override
    @Transactional
    public ProductPhoto setMainPhoto(Long tenantId, Long productId, Long photoId) {
        log.info("Setting photo {} as main for product {} (tenant {})", photoId, productId, tenantId);

        ProductPhoto newMainPhoto = productPhotoRepository.findByTenantIdAndId(tenantId, photoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductPhoto",
                        "id",
                        photoId,
                        tenantId
                ));

        if (!newMainPhoto.getProduct().getId().equals(productId)) {
            log.error("Attempt to set main photo failed: Photo {} does not belong to product {}.", photoId, productId);
            throw new BadRequestException("Photo with ID " + photoId +
                                                  " does not belong to product with ID " + productId);
        }

        if (newMainPhoto.isMain()) {
            log.debug("Photo {} is already the main photo for product {}. No changes needed.", photoId, productId);
            return newMainPhoto;
        }

        if (!productRepository.existsByTenantIdAndId(tenantId, productId)) {
            log.error(
                    "Consistency issue: Product {} not found for tenant {}, but photo {} exists.",
                    productId, tenantId, photoId
            );
            throw new ResourceNotFoundException("Product", "id", productId, tenantId);
        }

        unsetCurrentMainPhoto(tenantId, productId);

        newMainPhoto.setMain(true);
        ProductPhoto savedPhoto = productPhotoRepository.save(newMainPhoto);
        log.info("Photo {} is now the main photo for product {} (tenant {})", photoId, productId, tenantId);
        return savedPhoto;
    }

    private void unsetCurrentMainPhoto(Long tenantId, Long productId) {
        Optional<ProductPhoto> currentMainPhotoOpt = productPhotoRepository.findByTenantIdAndProductIdAndMainTrue(
                tenantId,
                productId
        );
        if (currentMainPhotoOpt.isPresent()) {
            ProductPhoto currentMainPhoto = currentMainPhotoOpt.get();
            currentMainPhoto.setMain(false);
            productPhotoRepository.save(currentMainPhoto);
            log.debug("Unset main flag for previous main photo {} of product {}", currentMainPhoto.getId(), productId);
        } else {
            log.debug("No existing main photo found for product {}", productId);
        }
    }
}
