package diploma.ecommerce.backend.shopbase.service;

import java.util.List;

import diploma.ecommerce.backend.shopbase.model.ProductPhoto;

public interface ProductPhotoService {

    List<ProductPhoto> getPhotosByProductId(Long tenantId, Long productId);

    ProductPhoto addPhoto(Long tenantId, Long productId, ProductPhoto photo);

    void deletePhoto(Long tenantId, Long photoId, Long productId);

    ProductPhoto setMainPhoto(Long tenantId, Long productId, Long photoId);
}
