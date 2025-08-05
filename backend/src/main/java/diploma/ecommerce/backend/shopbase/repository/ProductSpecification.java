package diploma.ecommerce.backend.shopbase.repository;

import diploma.ecommerce.backend.shopbase.model.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> hasTenantId(Long tenantId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<Product> nameContainsIgnoreCase(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Product> isActive(boolean active) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }
}
