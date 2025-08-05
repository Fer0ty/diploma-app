package diploma.ecommerce.backend.shopbase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT) // 409 Conflict
public class StatusNameAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StatusNameAlreadyExistsException(String message) {
        super(message);
    }
}
