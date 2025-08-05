package diploma.ecommerce.backend.shopbase.controller;

import java.net.URI;
import java.util.List;

import diploma.ecommerce.backend.shopbase.dto.mapper.ProductMapper;
import diploma.ecommerce.backend.shopbase.dto.mapper.ProductPhotoMapper;
import diploma.ecommerce.backend.shopbase.dto.record.ProductSearchCriteria;
import diploma.ecommerce.backend.shopbase.dto.request.ProductCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.ProductPhotoCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.ProductUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ProductPhotoResponse;
import diploma.ecommerce.backend.shopbase.dto.response.ProductResponse;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductPhoto;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.ProductPhotoService;
import diploma.ecommerce.backend.shopbase.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "API для управления продуктами и их фотографиями (в контексте тенанта)")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ProductPhotoService productPhotoService;
    private final ProductPhotoMapper productPhotoMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    // Управление Продуктами

    @Operation(summary = "Получить список продуктов (с пагинацией и фильтрацией)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список продуктов получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Поиск по части названия (без учета регистра)") @RequestParam(required = false) String nameLike,
            @Parameter(description = "Фильтр по категории") @RequestParam(required = false) String category,
            @Parameter(description = "Фильтр по активности (true/false)") @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ProductSearchCriteria criteria = new ProductSearchCriteria(nameLike, category, active);
        log.debug("Fetching products for tenant {} with criteria: {}", tenantId, criteria);

        Page<ProductResponse> productPage = productService.findProducts(tenantId, criteria, pageable)
                .map(productMapper::toProductResponse);

        log.info(
                "Returning {} products page {}/{}",
                productPage.getNumberOfElements(),
                productPage.getNumber(),
                productPage.getTotalPages()
        );
        return ResponseEntity.ok(productPage);
    }

    @Operation(summary = "Получить продукт по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт найден"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID продукта") @PathVariable Long productId
    ) {
        log.debug("Request to get product {} for tenant {}", productId, getCurrentTenantIdOrFail());
        Long tenantId = getCurrentTenantIdOrFail();
        ProductResponse product = productMapper.toProductResponse(
                productService.getProductById(tenantId, productId)
        );
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Создать новый продукт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Продукт успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Данные для создания продукта") @Valid @RequestBody ProductCreateRequest request
    ) {
        log.info("Request to create product for tenant {}: {}", getCurrentTenantIdOrFail(), request.getName());
        Long tenantId = getCurrentTenantIdOrFail();
        Product createdProductEntity = productService.createProduct(tenantId, productMapper.toProduct(request));
        ProductResponse createdProductResponse = productMapper.toProductResponse(createdProductEntity);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProductResponse.getId())
                .toUri();

        log.info("Product {} created successfully", createdProductResponse.getId());
        return ResponseEntity.created(location).body(createdProductResponse);
    }

    @Operation(summary = "Обновить существующий продукт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID продукта для обновления") @PathVariable Long productId,
            @Parameter(description = "Данные для обновления") @Valid @RequestBody ProductUpdateRequest request
    ) {
        log.info("Request to update product {} for tenant {}", productId, getCurrentTenantIdOrFail());
        Long tenantId = getCurrentTenantIdOrFail();
        Product updatedProductEntity = productService.updateProduct(
                tenantId,
                productId,
                productMapper.toProduct(request)
        );
        ProductResponse updatedProductResponse = productMapper.toProductResponse(updatedProductEntity);
        log.info("Product {} updated successfully", productId);
        return ResponseEntity.ok(updatedProductResponse);
    }

    @Operation(summary = "Удалить продукт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Продукт успешно удален (или деактивирован)"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить продукт (связан с заказами)")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID продукта для удаления") @PathVariable Long productId
    ) {
        log.warn("Request to delete product {} for tenant {}", productId, getCurrentTenantIdOrFail());
        Long tenantId = getCurrentTenantIdOrFail();
        productService.deleteProduct(tenantId, productId);
        log.info("Product {} deleted/deactivated successfully for tenant {}", productId, tenantId);
        return ResponseEntity.noContent().build();
    }

    // Управление Фото Продукта

    @Operation(summary = "Получить все фотографии для продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список фотографий получен"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    @GetMapping("/{productId}/photos")
    public ResponseEntity<List<ProductPhotoResponse>> getProductPhotos(
            @Parameter(description = "ID продукта") @PathVariable Long productId
    ) {
        log.debug("Request to get photos for product {} (tenant {})", productId, getCurrentTenantIdOrFail());
        Long tenantId = getCurrentTenantIdOrFail();
        List<ProductPhotoResponse> photos = productPhotoService.getPhotosByProductId(tenantId, productId)
                .stream()
                .map(productPhotoMapper::toProductPhotoResponse)
                .toList();
        return ResponseEntity.ok(photos);
    }

    @Operation(summary = "Добавить фотографию к продукту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Фотография успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    @PostMapping("/{productId}/photos")
    public ResponseEntity<ProductPhotoResponse> addProductPhoto(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @Parameter(description = "Данные фотографии") @Valid @RequestBody ProductPhotoCreateRequest request
    ) {
        log.info(
                "Request to add photo to product {} (tenant {}): path={}",
                productId,
                getCurrentTenantIdOrFail(),
                request.getFilePath()
        );
        Long tenantId = getCurrentTenantIdOrFail();
        ProductPhoto createdPhotoEntity = productPhotoService.addPhoto(
                tenantId,
                productId,
                productPhotoMapper.toProductPhoto(request)
        );
        ProductPhotoResponse createdPhotoResponse = productPhotoMapper.toProductPhotoResponse(createdPhotoEntity);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path(
                "/api/v1/products/{productId}/photos/{photoId}").buildAndExpand(
                productId,
                createdPhotoResponse.getId()
        ).toUri();
        log.info("Photo {} added successfully to product {}", createdPhotoResponse.getId(), productId);
        return ResponseEntity.created(location).body(createdPhotoResponse);
    }

    @Operation(summary = "Удалить фотографию продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Фотография успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Фотография или продукт не найдены")
    })
    @DeleteMapping("/{productId}/photos/{photoId}")
    public ResponseEntity<Void> deleteProductPhoto(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @Parameter(description = "ID фотографии для удаления") @PathVariable Long photoId
    ) {
        log.warn(
                "Request to delete photo {} for product {} (tenant {})",
                photoId,
                productId,
                getCurrentTenantIdOrFail()
        );
        Long tenantId = getCurrentTenantIdOrFail();
        productPhotoService.deletePhoto(tenantId, productId, photoId);
        log.info("Photo {} deleted successfully from product {}", photoId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Установить фотографию как главную для продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография установлена как главная"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Продукт или фотография не найдены")
    })
    @PutMapping("/{productId}/photos/{photoId}/set-main")
    public ResponseEntity<ProductPhotoResponse> setMainProductPhoto(
            @Parameter(description = "ID продукта") @PathVariable Long productId,
            @Parameter(description = "ID фотографии") @PathVariable Long photoId
    ) {
        log.info(
                "Request to set photo {} as main for product {} (tenant {})",
                photoId,
                productId,
                getCurrentTenantIdOrFail()
        );
        Long tenantId = getCurrentTenantIdOrFail();
        ProductPhoto mainPhotoEntity = productPhotoService.setMainPhoto(tenantId, productId, photoId);
        ProductPhotoResponse mainPhotoResponse = productPhotoMapper.toProductPhotoResponse(mainPhotoEntity);
        log.info("Photo {} set as main successfully for product {}", photoId, productId);
        return ResponseEntity.ok(mainPhotoResponse);
    }
}
