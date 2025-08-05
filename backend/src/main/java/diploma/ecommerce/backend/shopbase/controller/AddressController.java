package diploma.ecommerce.backend.shopbase.controller;

import java.net.URI;
import java.util.List;

import diploma.ecommerce.backend.shopbase.dto.mapper.AddressMapper;
import diploma.ecommerce.backend.shopbase.dto.request.AddressCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.AddressUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.AddressResponse;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "API для управления адресами клиентов (в контексте тенанта)")
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    @Operation(summary = "Получить все адреса для текущего тенанта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список адресов получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        Long tenantId = getCurrentTenantIdOrFail();
        List<AddressResponse> addresses = addressService.getAllAddresses(tenantId)
                .stream()
                .map(addressMapper::toAddressResponse)
                .toList();
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Получить адрес по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Адрес найден"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден для данного тенанта")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddressById(
            @Parameter(description = "ID адреса") @PathVariable Long id
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        AddressResponse address = addressMapper.toAddressResponse(
                addressService.getAddress(tenantId, id)
        );
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Создать новый адрес")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Адрес успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (ошибка валидации)")
    })
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Parameter(description = "Данные для создания адреса") @Valid @RequestBody AddressCreateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        AddressResponse createdAddress = addressMapper.toAddressResponse(
                addressService.createAddress(tenantId, addressMapper.toAddress(request))
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAddress.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdAddress);
    }

    @Operation(summary = "Обновить существующий адрес")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Адрес успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(description = "ID адреса для обновления") @PathVariable Long id,
            @Parameter(description = "Данные для обновления") @Valid @RequestBody AddressUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        AddressResponse updatedAddress = addressMapper.toAddressResponse(
                addressService.updateAddress(tenantId, id, addressMapper.toAddress(request))
        );
        return ResponseEntity.ok(updatedAddress);
    }

    @Operation(summary = "Удалить адрес")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Адрес успешно удален"),
            @ApiResponse(responseCode = "404", description = "Адрес не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "ID адреса для удаления") @PathVariable Long id
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        addressService.deleteAddress(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
