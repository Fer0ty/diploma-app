package diploma.ecommerce.backend.shopbase.exception;

import lombok.Getter;

@Getter
public class UniquenessViolationException extends RuntimeException {
    private final String entity;
    private final String field;
    private final Object value;

    public UniquenessViolationException(String entity, String field, Object value) {
        super(String.format("%s with %s '%s' already exists", entity, field, value));
        this.entity = entity;
        this.field = field;
        this.value = value;
    }
}
