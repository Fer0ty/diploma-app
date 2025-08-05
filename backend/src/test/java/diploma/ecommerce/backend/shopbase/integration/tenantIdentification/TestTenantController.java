package diploma.ecommerce.backend.shopbase.integration.tenantIdentification;

import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-tenant-api")
public class TestTenantController {

    @GetMapping(value = "/current-tenant-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantIdResponse> getCurrentTenantId() {
        return ResponseEntity.ok(new TenantIdResponse(TenantContext.getTenantId()));
    }

    @GetMapping(value = "/protected-echo-tenant-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantIdResponse> protectedEchoTenantId() {
        return ResponseEntity.ok(new TenantIdResponse(TenantContext.getTenantId()));
    }

    public record TenantIdResponse(Long tenantId) {
    }
}

