package diploma.ecommerce.backend.shopbase.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Map<String, Object> buildErrorResponseBody(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        try {
            body.put("path", request.getDescription(false).replace("uri=", ""));
        } catch (Exception e) {
            body.put("path", "unknown");
        }
        return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request
        );
        responseBody.put("fieldErrors", fieldErrors);
        log.warn("Validation failed: {}", fieldErrors);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
        responseBody.put("resource", ex.getResourceName());
        responseBody.put("field", ex.getFieldName());
        responseBody.put("value", ex.getFieldValue());
        if (ex.getTenantId() != null) {
            responseBody.put("tenantId", ex.getTenantId());
        }
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UniquenessViolationException.class)
    public ResponseEntity<Map<String, Object>> handleUniquenessViolationException(
            UniquenessViolationException ex, WebRequest request) {
        log.warn("Uniqueness violation: {}", ex.getMessage());
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
        responseBody.put("entity", ex.getEntity());
        responseBody.put("field", ex.getField());
        responseBody.put("value", ex.getValue());
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            InsufficientStockException.class,
            DataIntegrityViolationException.class,
            StatusNameAlreadyExistsException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflictExceptions(
            RuntimeException ex, WebRequest request) {
        log.warn("Conflict or data integrity issue: {}", ex.getMessage());
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred. Please contact support.",
                request
        );
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.warn(
                "Authentication failed for request [{}]: {}",
                request.getDescription(false),
                ex.getMessage()
        );
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            String field = result.getMethodParameter().getParameterName();
            String message = result.getResolvableErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            fieldErrors.put(field != null ? field : "unknown", message);
        });

        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.BAD_REQUEST,
                "Validation failure",
                request
        );
        responseBody.put("fieldErrors", fieldErrors);

        log.warn("Method parameter validation failed: {}", fieldErrors);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BadRequestException> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex
    ) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        BadRequestException error = new BadRequestException(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
