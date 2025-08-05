package diploma.ecommerce.backend.shopbase.integration.yookassa;
//
//import diploma.ecommerce.backend.shopbase.dto.response.PaymentResponse;
//import diploma.ecommerce.backend.shopbase.exception.PaymentExceptions;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.hibernate.validator.constraints.URL;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/payments")
//@Validated
//@Slf4j
public class PaymentController {
//
//    private final PaymentService paymentService;
//
//    public PaymentController(PaymentService paymentService) {
//        this.paymentService = paymentService;
//    }
//
//    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<PaymentResponse> createPayment(
//            @Valid @RequestBody CreatePaymentDto request) {
//
//        try {
//            PaymentResponse response = paymentService.createPaymentForOrder(
//                    request.getOrderId(),
//                    request.getReturnUrl()
//            );
//            return ResponseEntity.ok(response);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/{paymentId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
//        try {
//            PaymentResponse response = paymentService.getPaymentInfo(paymentId);
//            return ResponseEntity.ok(response);
//        } catch (PaymentExceptions.PaymentNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestBody PaymentResponse notification,
//            HttpServletRequest request) {
//
//        try {
//            // В production здесь должна быть проверка подписи webhook'а
//            log.info("Received payment webhook for payment ID: {}", notification.getId());
//
//            paymentService.processPaymentNotification(notification);
//
//            return ResponseEntity.ok("OK");
//        } catch (Exception e) {
//            log.error("Error processing payment webhook", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
//        }
//    }
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class CreatePaymentDto {
//        @NotNull
//        private Long orderId;
//
//        @NotBlank
//        @URL
//        private String returnUrl;
//    }
}
