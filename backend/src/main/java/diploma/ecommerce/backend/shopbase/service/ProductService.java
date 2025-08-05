package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.dto.record.ProductSearchCriteria;
import diploma.ecommerce.backend.shopbase.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<Product> getAllProducts(Long tenantId, Pageable pageable);

    Page<Product> getActiveProducts(Long tenantId, Pageable pageable);

    Product getProductById(Long tenantId, Long id);

    Product createProduct(Long tenantId, Product product);

    Product updateProduct(Long tenantId, Long id, Product productDetails);

    void deleteProduct(Long tenantId, Long id);

    Page<Product> findProducts(Long tenantId, ProductSearchCriteria criteria, Pageable pageable);

    Page<Product> getProductsByActiveStatus(Long tenantId, boolean active, Pageable pageable);
}
