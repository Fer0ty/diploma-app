package diploma.ecommerce.backend.shopbase.integration.yookassa;

//import diploma.ecommerce.backend.shopbase.config.YooKassaConfig;
//import diploma.ecommerce.backend.shopbase.dto.request.CreatePaymentRequest;
//import diploma.ecommerce.backend.shopbase.dto.response.PaymentResponse;
//import diploma.ecommerce.backend.shopbase.exception.PaymentExceptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.HttpServerErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.UUID;
//
//@Component
//@Slf4j
public class YooKassaApiClient {
//
//    private final RestTemplate restTemplate;
//    private final YooKassaConfig config;
//
//    public YooKassaApiClient(@Qualifier("yooKassaRestTemplate") RestTemplate restTemplate,
//                             YooKassaConfig config) {
//        this.restTemplate = restTemplate;
//        this.config = config;
//    }
//
//    public PaymentResponse createPayment(CreatePaymentRequest request) {
//        try {
//            String url = config.getApiUrl() + "/payments";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Idempotence-Key", UUID.randomUUID().toString());
//
//            HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request, headers);
//
//            log.info("Creating payment for amount: {} {}",
//                    request.getAmount().getValue(),
//                    request.getAmount().getCurrency());
//
//            ResponseEntity<PaymentResponse> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    PaymentResponse.class
//            );
//
//            PaymentResponse paymentResponse = response.getBody();
//            log.info("Payment created successfully with ID: {}", paymentResponse.getId());
//
//            return paymentResponse;
//
//        } catch (HttpClientErrorException e) {
//            log.error("Client error while creating payment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new PaymentExceptions.PaymentException("Failed to create payment: " + e.getMessage(), e);
//        } catch (HttpServerErrorException e) {
//            log.error("Server error while creating payment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new PaymentExceptions.PaymentException("YooKassa server error: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Unexpected error while creating payment", e);
//            throw new PaymentExceptions.PaymentException("Unexpected error during payment creation", e);
//        }
//    }
//
//    public PaymentResponse getPayment(String paymentId) {
//        try {
//            String url = config.getApiUrl() + "/payments/" + paymentId;
//
//            log.info("Getting payment info for ID: {}", paymentId);
//
//            ResponseEntity<PaymentResponse> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    null,
//                    PaymentResponse.class
//            );
//
//            PaymentResponse paymentResponse = response.getBody();
//            log.info("Payment info retrieved successfully for ID: {}, status: {}",
//                    paymentId, paymentResponse.getStatus());
//
//            return paymentResponse;
//
//        } catch (HttpClientErrorException e) {
//            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
//                log.warn("Payment not found: {}", paymentId);
//                throw new PaymentExceptions.PaymentNotFoundException("Payment not found: " + paymentId);
//            }
//            log.error("Client error while getting payment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new PaymentExceptions.PaymentException("Failed to get payment: " + e.getMessage(), e);
//        } catch (HttpServerErrorException e) {
//            log.error("Server error while getting payment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new PaymentExceptions.PaymentException("YooKassa server error: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Unexpected error while getting payment", e);
//            throw new PaymentExceptions.PaymentException("Unexpected error during payment retrieval", e);
//        }
//    }
}
