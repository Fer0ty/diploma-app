package diploma.ecommerce.backend.shopbase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при попытке использовать email, который уже существует
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String email;
    private final Long tenantId;

    public EmailAlreadyExistsException(String message) {
        super(message);
        this.email = null;
        this.tenantId = null;
    }

    public EmailAlreadyExistsException(String email, Long tenantId) {
        super(String.format("Email '%s' is already registered for Tenant ID: %d.", email, tenantId));
        this.email = email;
        this.tenantId = tenantId;
    }

    public String getEmail() {
        return email;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
