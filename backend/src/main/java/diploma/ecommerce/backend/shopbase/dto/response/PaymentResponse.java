package diploma.ecommerce.backend.shopbase.dto.response;

import diploma.ecommerce.backend.shopbase.dto.request.CreatePaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String status;
    private CreatePaymentRequest.Amount amount;
    private String description;
    private Recipient recipient;
    private PaymentMethod paymentMethod;
    private String capturedAt;
    private String createdAt;
    private String expiresAt;
    private CreatePaymentRequest.Confirmation confirmation;
    private boolean test;
    private boolean paid;
    private boolean refundable;
    private CreatePaymentRequest.Metadata metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recipient {
        private String accountId;
        private String gatewayId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethod {
        private String type;
        private String id;
        private boolean saved;
        private String title;
        private Card card;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Card {
            private String first6;
            private String last4;
            private String expiryMonth;
            private String expiryYear;
            private String cardType;
            private String issuerCountry;
            private String issuerName;
        }
    }

    public enum PaymentStatus {
        PENDING("pending"),
        WAITING_FOR_CAPTURE("waiting_for_capture"),
        SUCCEEDED("succeeded"),
        CANCELED("canceled"),
        WAITING_FOR_PAYMENT("waiting_for_payment");

        private final String status;

        PaymentStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
