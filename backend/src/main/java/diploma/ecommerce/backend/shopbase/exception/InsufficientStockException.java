package diploma.ecommerce.backend.shopbase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при недостатке товара на складе для выполнения операции.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(Long productId, int requestedQuantity, int availableQuantity) {
        super(String.format(
                "Insufficient stock for Product ID: %d. Requested: %d, Available: %d.",
                productId, requestedQuantity, availableQuantity
        ));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
}
