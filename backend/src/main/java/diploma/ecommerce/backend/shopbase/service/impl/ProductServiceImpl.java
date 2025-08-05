package diploma.ecommerce.backend.shopbase.service.impl;

import java.util.List;

import diploma.ecommerce.backend.shopbase.dto.record.ProductSearchCriteria;
import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.OrderItemRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductSpecification;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final OrderItemRepository orderItemRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return productRepository.findAllByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getActiveProducts(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return productRepository.findByTenantIdAndActive(tenantId, true, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByActiveStatus(Long tenantId, boolean active, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return productRepository.findByTenantIdAndActive(tenantId, active, pageable);
    }


    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long tenantId, Long id) {
        Product product = productRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id, tenantId));
        log.debug(
                "Fetched product {} for tenant {}. Photos count: {}",
                id,
                tenantId,
                product.getPhotos() != null ? product.getPhotos().size() : 0
        );
        return product;
    }

    @Override
    @Transactional
    public Product createProduct(Long tenantId, Product product) {
        log.info("Creating product '{}' for tenant {}", product.getName(), tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        product.setTenant(tenant);
        product.setId(null);

        Product savedProduct = productRepository.save(product);
        log.info("Product {} created successfully for tenant {}", savedProduct.getId(), tenantId);
        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(
            Long tenantId,
            Long id,
            Product productDetails
    ) {
        log.info("Updating product {} for tenant {}", id, tenantId);
        Product existingProduct = productRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id, tenantId));

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setCategory(productDetails.getCategory());
        if (productDetails.getActive() != null) {
            existingProduct.setActive(productDetails.getActive());
        }


        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product {} updated successfully for tenant {}", id, tenantId);
        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(Long tenantId, Long id) {
        log.warn("Attempting to delete product {} for tenant {}", id, tenantId);

        if (!productRepository.existsByTenantIdAndId(tenantId, id)) {
            throw new ResourceNotFoundException("Product", "id", id, tenantId);
        }

        List<OrderItem> relatedOrderItems = orderItemRepository.findByTenantIdAndProductId(tenantId, id);
        if (!relatedOrderItems.isEmpty()) {
            log.error(
                    "Cannot delete product {} (tenant {}): it is referenced in {} order item(s). First Order ID: {}",
                    id, tenantId, relatedOrderItems.size(), relatedOrderItems.get(0).getOrder().getId()
            );
            throw new DataIntegrityViolationException(
                    "Cannot delete product with ID " + id + " because it is associated with existing orders."
            );
        }
        productRepository.deleteByTenantIdAndId(tenantId, id);
        log.info("Product {} deleted successfully for tenant {}", id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findProducts(Long tenantId, ProductSearchCriteria criteria, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }

        Specification<Product> spec = Specification.where(ProductSpecification.hasTenantId(tenantId));

        if (criteria.nameLike() != null && !criteria.nameLike().isBlank()) {
            spec = spec.and(ProductSpecification.nameContainsIgnoreCase(criteria.nameLike()));
        }
        if (criteria.category() != null && !criteria.category().isBlank()) {
            spec = spec.and(ProductSpecification.hasCategory(criteria.category()));
        }
        if (criteria.active() != null) {
            spec = spec.and(ProductSpecification.isActive(criteria.active()));
        }

        log.debug("Finding products for tenant {} with criteria {} and pageable {}", tenantId, criteria, pageable);
        return productRepository.findAll(spec, pageable);
    }
}
