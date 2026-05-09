package SpringClass.shop.service;

import SpringClass.shop.dto.Payments.request.*;
import SpringClass.shop.dto.Payments.response.*;
import SpringClass.shop.entity.Orders.OrderItems;
import SpringClass.shop.entity.Orders.Orders;
import SpringClass.shop.entity.Payments.Payments;
import SpringClass.shop.entity.Products.Products;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private OrdersRepository ordersRepository;
    @Mock private PaymentsRepository paymentsRepository;
    @Mock private ProductsRepository productsRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private PaymentService paymentService;

    private Users buyer;
    private Users sellerUser;
    private Sellers seller;
    private Products product;
    private Orders order;
    private OrderItems orderItem;
    private Payments payment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "secretKey", "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R");
        ReflectionTestUtils.setField(paymentService, "tossBaseUrl", "https://api.tosspayments.com/v1/payments");

        buyer = Users.builder()
                .id(1L).email("buyer@test.com").password("pw")
                .nickname("구매자").gender(GenderRole.M).userRole(UserRole.USER).build();

        sellerUser = Users.builder()
                .id(2L).email("seller@test.com").password("pw")
                .nickname("판매자").gender(GenderRole.M).userRole(UserRole.SELLER).build();

        seller = Sellers.builder().id(1L).user(sellerUser).storeName("테스트샵").build();

        product = Products.builder()
                .id(1L).name("테스트 상품").price(new BigDecimal("10000")).stock(10)
                .seller(seller).build();

        order = Orders.builder()
                .id(1L).user(buyer).orderStatus(OrderStatus.WAITING)
                .totalPrice(new BigDecimal("10000")).address("서울시 강남구")
                .createdAt(LocalDateTime.now()).build();

        orderItem = OrderItems.builder()
                .id(1L).order(order).product(product).quantity(1)
                .itemPrice(new BigDecimal("10000")).totalPrice(new BigDecimal("10000")).build();

        ReflectionTestUtils.setField(order, "items", List.of(orderItem));

        payment = Payments.builder()
                .id(1L).order(order).user(buyer)
                .paymentMethod("PENDING").paymentStatus("PENDING")
                .amount(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    // ── ready ────────────────────────────────────────────────────────

    @Test
    @DisplayName("결제 준비 성공 시 tossOrderId와 orderName 반환")
    void ready_success() {
        PaymentReadyRequest request = new PaymentReadyRequest();
        ReflectionTestUtils.setField(request, "orderId", 1L);

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        when(paymentsRepository.save(any())).thenReturn(payment);

        PaymentReadyResponse response = paymentService.ready(request);

        assertThat(response.getTossOrderId()).isNotBlank();
        assertThat(response.getOrderName()).isEqualTo("테스트 상품");
        assertThat(response.getAmount()).isEqualTo(10000L);
        assertThat(response.getCustomerName()).isEqualTo("구매자");
        verify(paymentsRepository).save(argThat(p -> p.getTossOrderId() != null));
    }

    @Test
    @DisplayName("여러 상품 주문 시 orderName이 '상품명 외 N건' 형식으로 반환")
    void ready_multipleItems_orderNameContainsExtra() {
        Products product2 = Products.builder().id(2L).name("두번째 상품").price(new BigDecimal("5000")).stock(5).seller(seller).build();
        OrderItems orderItem2 = OrderItems.builder().id(2L).order(order).product(product2).quantity(1)
                .itemPrice(new BigDecimal("5000")).totalPrice(new BigDecimal("5000")).build();
        ReflectionTestUtils.setField(order, "items", List.of(orderItem, orderItem2));

        PaymentReadyRequest request = new PaymentReadyRequest();
        ReflectionTestUtils.setField(request, "orderId", 1L);

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        when(paymentsRepository.save(any())).thenReturn(payment);

        PaymentReadyResponse response = paymentService.ready(request);

        assertThat(response.getOrderName()).isEqualTo("테스트 상품 외 1건");
    }

    @Test
    @DisplayName("이미 완료된 결제에 준비 요청 시 TossPaymentException 발생")
    void ready_alreadyDone_throwsTossPaymentException() {
        payment.setPaymentStatus("DONE");

        PaymentReadyRequest request = new PaymentReadyRequest();
        ReflectionTestUtils.setField(request, "orderId", 1L);

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(1L, buyer)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.ready(request))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("이미 완료된 결제입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 준비 요청 시 OrderNotFoundException 발생")
    void ready_orderNotFound_throwsOrderNotFoundException() {
        PaymentReadyRequest request = new PaymentReadyRequest();
        ReflectionTestUtils.setField(request, "orderId", 999L);

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findByIdAndUser(999L, buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.ready(request))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ── confirm ──────────────────────────────────────────────────────

    @Test
    @DisplayName("결제 승인 성공 시 주문 PAID, 결제 DONE으로 업데이트")
    void confirm_success() {
        payment.setTossOrderId("toss-uuid-001");

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "amount", 10000L);

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(tossResponse, "status", "DONE");
        ReflectionTestUtils.setField(tossResponse, "method", "카드");
        ReflectionTestUtils.setField(tossResponse, "totalAmount", 10000L);

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(), eq(TossPaymentResponse.class)))
                .thenReturn(ResponseEntity.ok(tossResponse));
        when(paymentsRepository.save(any())).thenReturn(payment);
        when(ordersRepository.save(any())).thenReturn(order);

        TossPaymentResponse result = paymentService.confirm(request);

        assertThat(result.getPaymentKey()).isEqualTo("toss_pk_1234");
        assertThat(result.getStatus()).isEqualTo("DONE");
        verify(paymentsRepository).save(argThat(p -> "DONE".equals(p.getPaymentStatus())));
        verify(ordersRepository).save(argThat(o -> o.getOrderStatus() == OrderStatus.PAID));
    }

    @Test
    @DisplayName("결제 금액 불일치 시 TossPaymentException 발생")
    void confirm_amountMismatch_throwsTossPaymentException() {
        payment.setTossOrderId("toss-uuid-001");

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "amount", 99999L); // 금액 불일치

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirm(request))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("결제 금액이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("타인 결제 승인 요청 시 ForbiddenException 발생")
    void confirm_otherUserPayment_throwsForbiddenException() {
        Users other = Users.builder().id(99L).email("other@test.com").password("pw")
                .nickname("타인").gender(GenderRole.W).userRole(UserRole.USER).build();
        payment.setTossOrderId("toss-uuid-001");

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "amount", 10000L);

        when(securityUtils.getCurrentUser()).thenReturn(other);
        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirm(request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("토스 API가 에러 코드 반환 시 TossPaymentException 발생")
    void confirm_tossApiError_throwsTossPaymentException() {
        payment.setTossOrderId("toss-uuid-001");

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "amount", 10000L);

        TossPaymentResponse errorResponse = new TossPaymentResponse();
        ReflectionTestUtils.setField(errorResponse, "code", "INVALID_CARD_NUMBER");
        ReflectionTestUtils.setField(errorResponse, "message", "카드 번호를 확인해주세요.");

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(), eq(TossPaymentResponse.class)))
                .thenReturn(ResponseEntity.ok(errorResponse));

        assertThatThrownBy(() -> paymentService.confirm(request))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("카드 번호를 확인해주세요.");
    }

    // ── cancel ───────────────────────────────────────────────────────

    @Test
    @DisplayName("결제 취소 성공 시 재고 복구 및 주문/결제 상태 업데이트")
    void cancel_success() {
        payment.setTransactionId("toss_pk_1234");
        payment.setPaymentStatus("DONE");
        order.setOrderStatus(OrderStatus.PAID);

        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "cancelReason", "고객 요청");

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(tossResponse, "status", "CANCELED");

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(), eq(TossPaymentResponse.class)))
                .thenReturn(ResponseEntity.ok(tossResponse));
        when(paymentsRepository.save(any())).thenReturn(payment);
        when(ordersRepository.save(any())).thenReturn(order);
        when(productsRepository.save(any())).thenReturn(product);

        TossPaymentResponse result = paymentService.cancel("toss_pk_1234", request);

        assertThat(result.getStatus()).isEqualTo("CANCELED");
        verify(paymentsRepository).save(argThat(p -> "CANCELED".equals(p.getPaymentStatus())));
        verify(ordersRepository).save(argThat(o -> o.getOrderStatus() == OrderStatus.CANCELLED));
        verify(productsRepository).save(argThat(p -> p.getStock() == 11)); // 10 + 1 복구
    }

    @Test
    @DisplayName("PAID 상태가 아닌 주문 취소 시 TossPaymentException 발생")
    void cancel_notPaidOrder_throwsTossPaymentException() {
        payment.setTransactionId("toss_pk_1234");
        // order.orderStatus = WAITING (기본값)

        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "cancelReason", "고객 요청");

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancel("toss_pk_1234", request))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("결제 완료 상태의 주문만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("권한 없는 사용자가 취소 요청 시 ForbiddenException 발생")
    void cancel_unauthorized_throwsForbiddenException() {
        Users other = Users.builder().id(99L).email("other@test.com").password("pw")
                .nickname("타인").gender(GenderRole.W).userRole(UserRole.USER).build();
        payment.setTransactionId("toss_pk_1234");
        order.setOrderStatus(OrderStatus.PAID);

        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "cancelReason", "고객 요청");

        when(securityUtils.getCurrentUser()).thenReturn(other);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancel("toss_pk_1234", request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("ADMIN은 타인 결제 취소 가능")
    void cancel_adminCanCancelOthers() {
        Users admin = Users.builder().id(99L).email("admin@test.com").password("pw")
                .nickname("관리자").gender(GenderRole.M).userRole(UserRole.ADMIN).build();
        payment.setTransactionId("toss_pk_1234");
        order.setOrderStatus(OrderStatus.PAID);

        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "cancelReason", "관리자 취소");

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        ReflectionTestUtils.setField(tossResponse, "status", "CANCELED");

        when(securityUtils.getCurrentUser()).thenReturn(admin);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(), eq(TossPaymentResponse.class)))
                .thenReturn(ResponseEntity.ok(tossResponse));
        when(paymentsRepository.save(any())).thenReturn(payment);
        when(ordersRepository.save(any())).thenReturn(order);
        when(productsRepository.save(any())).thenReturn(product);

        assertThatCode(() -> paymentService.cancel("toss_pk_1234", request))
                .doesNotThrowAnyException();
    }

    // ── fail ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("결제 실패 처리 시 paymentStatus FAILED, 재고 복구")
    void fail_success() {
        payment.setTossOrderId("toss-uuid-001");

        PaymentFailRequest request = new PaymentFailRequest();
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "code", "PAY_PROCESS_CANCELED");
        ReflectionTestUtils.setField(request, "message", "사용자가 결제를 취소했습니다.");

        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));
        when(paymentsRepository.save(any())).thenReturn(payment);
        when(productsRepository.save(any())).thenReturn(product);

        PaymentFailResponse response = paymentService.fail(request);

        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getPaymentStatus()).isEqualTo("FAILED");
        verify(paymentsRepository).save(argThat(p -> "FAILED".equals(p.getPaymentStatus())));
        verify(productsRepository).save(argThat(p -> p.getStock() == 11)); // 재고 복구
    }

    @Test
    @DisplayName("존재하지 않는 tossOrderId로 실패 처리 시 PaymentNotFoundException 발생")
    void fail_notFound_throwsPaymentNotFoundException() {
        PaymentFailRequest request = new PaymentFailRequest();
        ReflectionTestUtils.setField(request, "orderId", "invalid-uuid");
        ReflectionTestUtils.setField(request, "code", "PAY_PROCESS_CANCELED");
        ReflectionTestUtils.setField(request, "message", "취소");

        when(paymentsRepository.findByTossOrderId("invalid-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.fail(request))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    // ── getPaymentByOrder ─────────────────────────────────────────────

    @Test
    @DisplayName("본인 주문의 결제 정보 조회 성공")
    void getPaymentByOrder_asOwner_success() {
        payment.setTransactionId("toss_pk_1234");
        payment.setPaymentStatus("DONE");
        payment.setPaidAt(LocalDateTime.now());

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        PaymentByOrderResponse response = paymentService.getPaymentByOrder(1L);

        assertThat(response.getPaymentKey()).isEqualTo("toss_pk_1234");
        assertThat(response.getStatus()).isEqualTo("DONE");
        assertThat(response.getTotalAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("판매자도 해당 주문 결제 정보 조회 가능")
    void getPaymentByOrder_asSeller_success() {
        payment.setPaymentStatus("DONE");

        when(securityUtils.getCurrentUser()).thenReturn(sellerUser);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentsRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        assertThatCode(() -> paymentService.getPaymentByOrder(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("관련 없는 사용자가 조회 시 ForbiddenException 발생")
    void getPaymentByOrder_unauthorized_throwsForbiddenException() {
        Users stranger = Users.builder().id(99L).email("x@test.com").password("pw")
                .nickname("낯선이").gender(GenderRole.W).userRole(UserRole.USER).build();

        when(securityUtils.getCurrentUser()).thenReturn(stranger);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.getPaymentByOrder(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── getPaymentByKey ───────────────────────────────────────────────

    @Test
    @DisplayName("본인 결제 단건 조회 성공")
    void getPaymentByKey_asOwner_success() {
        payment.setTransactionId("toss_pk_1234");
        payment.setPaymentStatus("DONE");

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));

        PaymentDetailResponse response = paymentService.getPaymentByKey("toss_pk_1234");

        assertThat(response.getPaymentKey()).isEqualTo("toss_pk_1234");
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("DONE");
    }

    @Test
    @DisplayName("ADMIN은 타인 결제 단건 조회 가능")
    void getPaymentByKey_asAdmin_success() {
        Users admin = Users.builder().id(99L).email("admin@test.com").password("pw")
                .nickname("관리자").gender(GenderRole.M).userRole(UserRole.ADMIN).build();
        payment.setTransactionId("toss_pk_1234");
        payment.setPaymentStatus("DONE");

        when(securityUtils.getCurrentUser()).thenReturn(admin);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));

        assertThatCode(() -> paymentService.getPaymentByKey("toss_pk_1234"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 결제 단건 조회 시 ForbiddenException 발생")
    void getPaymentByKey_unauthorized_throwsForbiddenException() {
        Users other = Users.builder().id(99L).email("other@test.com").password("pw")
                .nickname("타인").gender(GenderRole.W).userRole(UserRole.USER).build();
        payment.setTransactionId("toss_pk_1234");

        when(securityUtils.getCurrentUser()).thenReturn(other);
        when(paymentsRepository.findByTransactionId("toss_pk_1234")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getPaymentByKey("toss_pk_1234"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("존재하지 않는 paymentKey 조회 시 PaymentNotFoundException 발생")
    void getPaymentByKey_notFound_throwsPaymentNotFoundException() {
        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTransactionId("invalid_key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByKey("invalid_key"))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    // ── Toss API 오류 응답 (HttpClientErrorException) ─────────────────

    @Test
    @DisplayName("토스 API 4xx 응답 시 에러 메시지 파싱 후 TossPaymentException 발생")
    void confirm_tossApiHttpClientError_throwsTossPaymentException() throws Exception {
        payment.setTossOrderId("toss-uuid-001");

        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", "toss_pk_1234");
        ReflectionTestUtils.setField(request, "orderId", "toss-uuid-001");
        ReflectionTestUtils.setField(request, "amount", 10000L);

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request",
                org.springframework.http.HttpHeaders.EMPTY,
                "{\"code\":\"EXCEED_MAX_DAILY_PAYMENT_COUNT\",\"message\":\"일일 결제 한도를 초과했습니다.\"}".getBytes(),
                StandardCharsets.UTF_8
        );

        TossPaymentResponse errorResponse = new TossPaymentResponse();
        ReflectionTestUtils.setField(errorResponse, "code", "EXCEED_MAX_DAILY_PAYMENT_COUNT");
        ReflectionTestUtils.setField(errorResponse, "message", "일일 결제 한도를 초과했습니다.");

        when(securityUtils.getCurrentUser()).thenReturn(buyer);
        when(paymentsRepository.findByTossOrderId("toss-uuid-001")).thenReturn(Optional.of(payment));
        when(restTemplate.postForEntity(anyString(), any(), eq(TossPaymentResponse.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(TossPaymentResponse.class))).thenReturn(errorResponse);

        assertThatThrownBy(() -> paymentService.confirm(request))
                .isInstanceOf(TossPaymentException.class)
                .hasMessage("일일 결제 한도를 초과했습니다.");
    }
}
