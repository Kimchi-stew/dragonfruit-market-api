package SpringClass.shop.service;

import SpringClass.shop.dto.Payments.request.PaymentCancelRequest;
import SpringClass.shop.dto.Payments.request.PaymentConfirmRequest;
import SpringClass.shop.dto.Payments.request.PaymentFailRequest;
import SpringClass.shop.dto.Payments.request.PaymentReadyRequest;
import SpringClass.shop.dto.Payments.response.*;
import SpringClass.shop.entity.Orders.OrderItems;
import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Payments.Payments;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.OrderStatus;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.order.OrderNotFoundException;
import SpringClass.shop.exceptions.payment.PaymentNotFoundException;
import SpringClass.shop.exceptions.payment.TossPaymentException;
import SpringClass.shop.repository.Orders.OrdersRepository;
import SpringClass.shop.repository.Payments.PaymentsRepository;
import SpringClass.shop.repository.Products.ProductsRepository;
import SpringClass.shop.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SecurityUtils securityUtils;
    private final OrdersRepository ordersRepository;
    private final PaymentsRepository paymentsRepository;
    private final ProductsRepository productsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.base-url}")
    private String tossBaseUrl;

    // 결제 준비: tossOrderId 생성 후 저장, 프론트에서 토스 SDK 초기화에 사용
    @Transactional
    public PaymentReadyResponse ready(PaymentReadyRequest request) {
        Users user = securityUtils.getCurrentUser();

        Orders order = ordersRepository.findByIdAndUser(request.getOrderId(), user)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));

        Payments payment = paymentsRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        if ("DONE".equals(payment.getPaymentStatus())) {
            throw new TossPaymentException("이미 완료된 결제입니다.");
        }

        String tossOrderId = UUID.randomUUID().toString();
        String orderName = buildOrderName(order);

        payment.setTossOrderId(tossOrderId);
        payment.setOrderName(orderName);
        paymentsRepository.save(payment);

        return PaymentReadyResponse.builder()
                .tossOrderId(tossOrderId)
                .orderName(orderName)
                .amount(order.getTotalPrice().longValue())
                .customerName(user.getNickname())
                .build();
    }

    // 결제 승인: 토스 confirm API 호출 → 주문/결제 상태 PAID/DONE으로 업데이트
    @Transactional
    public TossPaymentResponse confirm(PaymentConfirmRequest request) {
        Payments payment = paymentsRepository.findByTossOrderId(request.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        Users user = securityUtils.getCurrentUser();
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 결제만 승인할 수 있습니다.");
        }

        if (!payment.getAmount().equals(BigDecimal.valueOf(request.getAmount()))) {
            throw new TossPaymentException("결제 금액이 일치하지 않습니다.");
        }

        TossPaymentResponse tossResponse = callTossConfirm(request);

        if (tossResponse.getCode() != null) {
            throw new TossPaymentException(tossResponse.getMessage());
        }

        payment.setTransactionId(tossResponse.getPaymentKey());
        payment.setPaymentStatus("DONE");
        payment.setPaymentMethod(tossResponse.getMethod() != null ? tossResponse.getMethod() : "UNKNOWN");
        payment.setPaidAt(LocalDateTime.now());
        paymentsRepository.save(payment);

        Orders order = payment.getOrder();
        order.setOrderStatus(OrderStatus.PAID);
        ordersRepository.save(order);

        return tossResponse;
    }

    // 결제 취소: 토스 cancel API 호출 → 재고 복구, 주문/결제 상태 CANCELLED/CANCELED로 업데이트
    @Transactional
    public TossPaymentResponse cancel(String paymentKey, PaymentCancelRequest request) {
        Payments payment = paymentsRepository.findByTransactionId(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        Users user = securityUtils.getCurrentUser();
        if (!payment.getUser().getId().equals(user.getId()) && user.getUserRole() != UserRole.ADMIN) {
            throw new ForbiddenException("취소 권한이 없습니다.");
        }

        Orders order = payment.getOrder();
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new TossPaymentException("결제 완료 상태의 주문만 취소할 수 있습니다.");
        }

        TossPaymentResponse tossResponse = callTossCancel(paymentKey, request);

        if (tossResponse.getCode() != null) {
            throw new TossPaymentException(tossResponse.getMessage());
        }

        payment.setPaymentStatus("CANCELED");
        payment.setCanceledAt(LocalDateTime.now());
        paymentsRepository.save(payment);

        order.setOrderStatus(OrderStatus.CANCELLED);
        ordersRepository.save(order);

        restoreStock(order);

        return tossResponse;
    }

    // 결제 실패: 토스 실패 리다이렉트 URL에서 프론트가 호출 → 재고 복구
    @Transactional
    public PaymentFailResponse fail(PaymentFailRequest request) {
        Payments payment = paymentsRepository.findByTossOrderId(request.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        payment.setPaymentStatus("FAILED");
        paymentsRepository.save(payment);

        Orders order = payment.getOrder();
        restoreStock(order);

        return PaymentFailResponse.builder()
                .orderId(order.getId())
                .paymentStatus("FAILED")
                .build();
    }

    // 주문별 결제 정보 조회: 본인 주문 또는 해당 주문 상품의 판매자만 조회 가능
    public PaymentByOrderResponse getPaymentByOrder(Long orderId) {
        Users user = securityUtils.getCurrentUser();

        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        boolean isOwner = order.getUser().getId().equals(user.getId());
        boolean isSeller = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getUser().getId().equals(user.getId()));

        if (!isOwner && !isSeller) {
            throw new ForbiddenException("조회 권한이 없습니다.");
        }

        Payments payment = paymentsRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        return PaymentByOrderResponse.builder()
                .paymentKey(payment.getTransactionId())
                .method(payment.getPaymentMethod())
                .status(payment.getPaymentStatus())
                .totalAmount(payment.getAmount().longValue())
                .paidAt(payment.getPaidAt())
                .canceledAt(payment.getCanceledAt())
                .build();
    }

    // 결제 단건 조회: 본인 결제 건 또는 ADMIN만 조회 가능
    public PaymentDetailResponse getPaymentByKey(String paymentKey) {
        Users user = securityUtils.getCurrentUser();

        Payments payment = paymentsRepository.findByTransactionId(paymentKey)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        if (!payment.getUser().getId().equals(user.getId()) && user.getUserRole() != UserRole.ADMIN) {
            throw new ForbiddenException("조회 권한이 없습니다.");
        }

        return PaymentDetailResponse.builder()
                .paymentKey(payment.getTransactionId())
                .orderId(payment.getOrder().getId())
                .method(payment.getPaymentMethod())
                .status(payment.getPaymentStatus())
                .totalAmount(payment.getAmount().longValue())
                .approvedAt(payment.getPaidAt())
                .requestedAt(payment.getCreatedAt())
                .build();
    }

    // --- private helpers ---

    private String buildOrderName(Orders order) {
        List<OrderItems> items = order.getItems();
        if (items.isEmpty()) return "주문";
        String firstName = items.get(0).getProduct().getName();
        return items.size() > 1 ? firstName + " 외 " + (items.size() - 1) + "건" : firstName;
    }

    private void restoreStock(Orders order) {
        for (OrderItems item : order.getItems()) {
            Products product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productsRepository.save(product);
        }
    }

    private TossPaymentResponse callTossConfirm(PaymentConfirmRequest request) {
        String url = tossBaseUrl + "/confirm";
        HttpHeaders headers = createAuthHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        return doPost(url, headers, body);
    }

    private TossPaymentResponse callTossCancel(String paymentKey, PaymentCancelRequest request) {
        String url = tossBaseUrl + "/" + paymentKey + "/cancel";
        HttpHeaders headers = createAuthHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());
        if (request.getCancelAmount() != null) {
            body.put("cancelAmount", request.getCancelAmount());
        }

        return doPost(url, headers, body);
    }

    private TossPaymentResponse doPost(String url, HttpHeaders headers, Map<String, Object> body) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(url, entity, TossPaymentResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            try {
                return objectMapper.readValue(e.getResponseBodyAsString(), TossPaymentResponse.class);
            } catch (Exception ex) {
                throw new TossPaymentException("토스 결제 API 호출 실패: " + e.getMessage());
            }
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }
}
