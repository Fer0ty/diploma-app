package diploma.ecommerce.backend.shopbase.controller;

import java.net.URI;

import diploma.ecommerce.backend.shopbase.dto.mapper.UserMapper;
import diploma.ecommerce.backend.shopbase.dto.request.UserCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.UserUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.UserResponse;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "API для управления клиентами магазина (в контексте тенанта)")
public class UserController {

    @Qualifier("customerUserService")
    private final UserService userService;
    private final UserMapper userMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    @Operation(summary = "Получить всех клиентов (с пагинацией)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список клиентов получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        Page<UserResponse> userPage = userService.getAllUsers(tenantId, pageable)
                .map(userMapper::toUserResponse);
        return ResponseEntity.ok(userPage);
    }

    @Operation(summary = "Получить клиента по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент найден"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID клиента") @PathVariable Long userId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        UserResponse user = userMapper.toUserResponse(
                userService.getUserById(tenantId, userId)
        );
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать нового клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Клиент успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (ошибка валидации)"),
            @ApiResponse(responseCode = "409", description = "Клиент с таким email уже существует в этом магазине")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "Данные для создания клиента") @Valid @RequestBody UserCreateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        UserResponse createdUser = userMapper.toUserResponse(
                userService.createUser(tenantId, userMapper.toUser(request))
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    @Operation(summary = "Обновить существующего клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (ошибка валидации)"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "409", description = "Новый email уже занят")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID клиента для обновления") @PathVariable Long userId,
            @Parameter(description = "Данные для обновления") @Valid @RequestBody UserUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        UserResponse updatedUser = userMapper.toUserResponse(
                userService.updateUser(tenantId, userId, userMapper.toUser(request))
        );
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Удалить клиента (ОСТОРОЖНО)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Клиент успешно удален (или деактивирован)"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить клиента (связан с заказами)")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID клиента для удаления") @PathVariable Long userId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        userService.deleteUser(tenantId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Активировать клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент активирован"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден")
    })
    @PostMapping("/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(
            @Parameter(description = "ID клиента") @PathVariable Long userId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        UserResponse user = userMapper.toUserResponse(
                userService.activateUser(tenantId, userId)
        );
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Деактивировать клиента (мягкое удаление)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент деактивирован"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден")
    })
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @Parameter(description = "ID клиента") @PathVariable Long userId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        UserResponse user = userMapper.toUserResponse(
                userService.deactivateUser(tenantId, userId)
        );
        return ResponseEntity.ok(user);
    }

}
