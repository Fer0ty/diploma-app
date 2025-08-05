package diploma.ecommerce.backend.shopbase.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Amount amount;
    private String description;
    private Confirmation confirmation;
    private boolean capture;
    private String orderId;
    private Metadata metadata;
    private Receipt receipt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private String value;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Confirmation {
        private String type;
        private String returnUrl;
        private String confirmationUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String tenantId;
        private String customerEmail;
        private String orderId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Receipt {
        private Customer customer;
        private List<Item> items;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Customer {
            private String email;
            private String phone;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Item {
            private String description;
            private String quantity;
            private Amount amount;
            private VatCode vatCode;

            public enum VatCode {
                NO_VAT(1),
                VAT_0(2),
                VAT_10(3),
                VAT_20(4);

                private final int code;

                VatCode(int code) {
                    this.code = code;
                }

                public int getCode() {
                    return code;
                }
            }
        }
    }
}
