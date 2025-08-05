package diploma.ecommerce.backend.shopbase.controller;

import java.io.IOException;

import diploma.ecommerce.backend.shopbase.dto.response.FileUploadResponse;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "API для загрузки файлов")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    private static Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    @Operation(
            summary = "Загрузить файл в указанную категорию",
            description = "Загружает файл (изображение) в указанную категорию (logos, headers, products, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Файл успешно загружен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(value = "/upload/{category}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "Категория файла (logos, headers, products, etc.)", required = true)
            @PathVariable String category,

            @Parameter(description = "Файл для загрузки", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Long tenantId = getCurrentTenantIdOrFail();

        // Валидация категории
        if (!isValidCategory(category)) {
            return ResponseEntity.badRequest().body(
                    new FileUploadResponse(false, null, "Invalid category: " + category)
            );
        }

        // Валидация типа файла (только изображения)
        if (!isValidImageFile(file)) {
            return ResponseEntity.badRequest().body(
                    new FileUploadResponse(false, null, "Only image files are allowed")
            );
        }

        String fileUrl = fileStorageService.storeFile(file, tenantId, category);

        return ResponseEntity.ok(new FileUploadResponse(true, fileUrl, "File uploaded successfully"));
    }

    private static boolean isValidCategory(String category) {
        return category != null && (
                category.equals("logos") ||
                        category.equals("headers") ||
                        category.equals("products") ||
                        category.equals("banners") ||
                        category.equals("other")
        );
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType.startsWith("image/");
    }
}
