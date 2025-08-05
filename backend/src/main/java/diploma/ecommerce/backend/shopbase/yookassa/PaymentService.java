package diploma.ecommerce.backend.shopbase.integration.yookassa;

//import diploma.ecommerce.backend.shopbase.dto.request.CreatePaymentRequest;
//import diploma.ecommerce.backend.shopbase.dto.response.PaymentResponse;
//import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
//import diploma.ecommerce.backend.shopbase.model.Order;
//import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
//import diploma.ecommerce.backend.shopbase.service.OrderService;
//import diploma.ecommerce.backend.shopbase.service.OrderStatusService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Service
//@Slf4j
//@Transactional
public class PaymentService {

//    private final YooKassaApiClient yooKassaClient;
//    private final OrderService orderService;
//
//    private final OrderStatusService orderStatusService;
////    private final PaymentRepository paymentRepository;
//
//    public PaymentService(YooKassaApiClient yooKassaClient,
//                          OrderService orderService, OrderStatusService orderStatusService
////                          PaymentRepository paymentRepository
//    ) {
//        this.yooKassaClient = yooKassaClient;
//        this.orderService = orderService;
//        this.orderStatusService = orderStatusService;
////        this.paymentRepository = paymentRepository;
//    }
//
//    public PaymentResponse createPaymentForOrder(Long orderId, String returnUrl) {
//        Long tenantId = TenantContext.getTenantId();
//
//        // Получаем заказ
//        Order order = orderService.getOrderById(orderId, tenantId);
//        if (order == null) {
//            throw new ResourceNotFoundException("Order", "id", orderId);
//        }
//
//        if (PaymentResponse.PaymentStatus.PENDING.equals(order.getStatus().getStatusName())) {
//            throw new IllegalStateException("Order payment is already processed");
//        }
//
//        // Формируем запрос на создание платежа
//        CreatePaymentRequest request = CreatePaymentRequest.builder()
//                .amount(CreatePaymentRequest.Amount.builder()
//                        .value(order.getTotalAmount().toString())
//                        .currency("RUB")
//                        .build())
//                .description("Оплата заказа №" + order.getId())
//                .confirmation(CreatePaymentRequest.Confirmation.builder()
//                        .type("redirect")
//                        .returnUrl(returnUrl)
//                        .build())
//                .capture(true)
//                .orderId(orderId.toString())
//                .metadata(CreatePaymentRequest.Metadata.builder()
//                        .tenantId(tenantId.toString())
//                        .customerEmail(order.getCustomer().getEmail())
//                        .orderId(orderId.toString())
//                        .build())
//                .receipt(buildReceipt(order))
//                .build();
//
//        PaymentResponse response = yooKassaClient.createPayment(request);
//
////        savePaymentInfo(response, order);
//        var status = response.getStatus();
//        orderService.updateOrderStatus(
//                tenantId,
//                orderId,
//                orderStatusService.findOrderStatusByName(status).get().getId(),
//                "Обновление статуса оплаты"
//        );
//
//        log.info("Payment created for order {}: payment ID {}", orderId, response.getId());
//
//        return response;
//    }
//
//    public PaymentResponse getPaymentInfo(String paymentId) {
//        PaymentResponse response = yooKassaClient.getPayment(paymentId);
//
//        // Обновляем информацию в БД
////        updatePaymentInfo(response);
//
//        return response;
//    }
//
//    public void processPaymentNotification(PaymentResponse notification) {
//        Long tenantId = TenantContext.getTenantId();
//        log.info("Processing payment notification for payment ID: {}", notification.getId());
//
//        // Обновляем информацию о платеже
////        updatePaymentInfo(notification);
//
//        // Обновляем статус заказа
//        if (notification.getMetadata() != null && notification.getMetadata().getOrderId() != null) {
//            Long orderId = Long.parseLong(notification.getMetadata().getOrderId());
//            PaymentResponse.PaymentStatus status = mapYooKassaStatusToPaymentStatus(notification.getStatus());
//            orderService.updateOrderStatus(
//                    tenantId,
//                    orderId,
//                    orderStatusService.findOrderStatusByName(status.toString()).orElseThrow().getId(),
//                    "Обновлен по статусу заказа");
//            if (PaymentResponse.PaymentStatus.SUCCEEDED.equals(status)) {
////                orderService.confirmPrder(orderId);
//                log.info("Order {} confirmed after successful payment", orderId);
//            } else if (PaymentResponse.PaymentStatus.CANCELED.equals(status)) {
////                orderService.cancelOrder(orderId);
//                log.info("Order {} canceled due to payment failure", orderId);
//            }
//        }
//    }
//
//    private CreatePaymentRequest.Receipt buildReceipt(Order order) {
//        List<CreatePaymentRequest.Receipt.Item> items = order.getOrderItems().stream()
//                .map(item -> CreatePaymentRequest.Receipt.Item.builder()
//                        .description(item.getProduct().getName())
//                        .quantity(item.getQuantity().toString())
//                        .amount(CreatePaymentRequest.Amount.builder()
//                                .value(item.getUnitPrice().toString())
//                                .currency("RUB")
//                                .build())
//                        .vatCode(CreatePaymentRequest.Receipt.Item.VatCode.NO_VAT)
//                        .build())
//                .toList();
//
//        return CreatePaymentRequest.Receipt.builder()
//                .customer(CreatePaymentRequest.Receipt.Customer.builder()
//                        .email(order.getCustomer().getEmail())
//                        .phone(order.getCustomer().getPhone())
//                        .build())
//                .items(items)
//                .build();
//    }
//
////    private void savePaymentInfo(PaymentResponse response, Order order) {
////        Payment payment = Payment.builder()
////                .paymentId(response.getId())
////                .tenantId(TenantContext.getTenantId())
////                .orderId(order.getId())
////                .amount(new BigDecimal(response.getAmount().getValue()))
////                .currency(response.getAmount().getCurrency())
////                .status(mapYooKassaStatusToPaymentStatus(response.getStatus()))
////                .createdAt(parseDateTime(response.getCreatedAt()))
////                .expiresAt(parseDateTime(response.getExpiresAt()))
////                .confirmationUrl(response.getConfirmation() != null ?
////                        response.getConfirmation().getConfirmationUrl() : null)
////                .build();
////
////        paymentRepository.save(payment);
////    }
//
////    private void updatePaymentInfo(PaymentResponse response) {
////        Payment payment = paymentRepository.findByPaymentId(response.getId());
////        if (payment != null) {
////            payment.setStatus(mapYooKassaStatusToPaymentStatus(response.getStatus()));
////            if (response.getCapturedAt() != null) {
////                payment.setCapturedAt(parseDateTime(response.getCapturedAt()));
////            }
////            paymentRepository.save(payment);
////        }
////    }
////
////    private PaymentResponse.PaymentStatus mapYooKassaStatusToPaymentStatus(String yooKassaStatus) {
////        return switch (yooKassaStatus) {
////            case "pending" -> PaymentResponse.PaymentStatus.PENDING;
////            case "waiting_for_capture" -> PaymentResponse.PaymentStatus.WAITING_FOR_PAYMENT;
////            case "succeeded" -> PaymentResponse.PaymentStatus.SUCCEEDED;
////            case "canceled" -> PaymentResponse.PaymentStatus.CANCELED;
////            default -> PaymentResponse.PaymentStatus.PENDING;
////        };
////    }
////
////    private LocalDateTime parseDateTime(String dateTimeString) {
////        if (dateTimeString == null) return null;
////        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
////    }
}
