package diploma.ecommerce.backend.shopbase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при нарушении целостности данных на уровне приложения
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class DataIntegrityViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataIntegrityViolationException(String message) {
        super(message);
    }

    public DataIntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
