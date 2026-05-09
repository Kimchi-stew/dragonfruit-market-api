package SpringClass.shop.controller;

import SpringClass.shop.dto.Payments.request.*;
import SpringClass.shop.dto.Payments.response.*;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.order.OrderNotFoundException;
import SpringClass.shop.exceptions.payment.PaymentNotFoundException;
import SpringClass.shop.exceptions.payment.TossPaymentException;
import SpringClass.shop.global.GlobalApiResponseHandler;
import SpringClass.shop.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private PaymentService paymentService;
    @InjectMocks private PaymentController paymentController;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalApiResponseHandler())
                .build();
    }

    // ── POST /payments/ready ─────────────────────────────────────────

    @Test
    @DisplayName("결제 준비 성공 시 200과 tossOrderId 반환")
    void ready_success() throws Exception {
        PaymentReadyResponse response = PaymentReadyResponse.builder()
                .tossOrderId("test-uuid-001")
                .orderName("테스트 상품")
                .amount(10000L)
                .customerName("구매자")
                .build();

        when(paymentService.ready(any(PaymentReadyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new java.util.HashMap<>() {{ put("orderId", 1); }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tossOrderId").value("test-uuid-001"))
                .andExpect(jsonPath("$.data.orderName").value("테스트 상품"))
                .andExpect(jsonPath("$.data.amount").value(10000))
                .andExpect(jsonPath("$.message").value("결제 준비 완료"));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 준비 시 404 반환")
    void ready_orderNotFound_returns404() throws Exception {
        when(paymentService.ready(any())).thenThrow(new OrderNotFoundException("주문을 찾을 수 없습니다."));

        mockMvc.perform(post("/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 완료된 결제에 준비 요청 시 400 반환")
    void ready_alreadyDone_returns400() throws Exception {
        when(paymentService.ready(any())).thenThrow(new TossPaymentException("이미 완료된 결제입니다."));

        mockMvc.perform(post("/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 완료된 결제입니다."));
    }

    @Test
    @DisplayName("orderId 누락 시 400 반환")
    void ready_missingOrderId_returns400() throws Exception {
        mockMvc.perform(post("/payments/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /payments/confirm ───────────────────────────────────────

    @Test
    @DisplayName("결제 승인 성공 시 200과 TossPaymentResponse 반환")
    void confirm_success() throws Exception {
        TossPaymentResponse tossResponse = new TossPaymentResponse();
        org.springframework.test.util.ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss_pk_1234");
        org.springframework.test.util.ReflectionTestUtils.setField(tossResponse, "status", "DONE");
        org.springframework.test.util.ReflectionTestUtils.setField(tossResponse, "totalAmount", 10000L);

        when(paymentService.confirm(any(PaymentConfirmRequest.class))).thenReturn(tossResponse);

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentKey\":\"toss_pk_1234\",\"orderId\":\"toss-uuid-001\",\"amount\":10000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentKey").value("toss_pk_1234"))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.message").value("결제 승인 완료"));
    }

    @Test
    @DisplayName("결제 금액 불일치 시 400 반환")
    void confirm_amountMismatch_returns400() throws Exception {
        when(paymentService.confirm(any())).thenThrow(new TossPaymentException("결제 금액이 일치하지 않습니다."));

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentKey\":\"toss_pk_1234\",\"orderId\":\"toss-uuid-001\",\"amount\":99999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("결제 금액이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("타인 결제 승인 시 403 반환")
    void confirm_forbidden_returns403() throws Exception {
        when(paymentService.confirm(any())).thenThrow(new ForbiddenException("본인의 결제만 승인할 수 있습니다."));

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentKey\":\"toss_pk_1234\",\"orderId\":\"toss-uuid-001\",\"amount\":10000}"))
                .andExpect(status().isForbidden());
    }

    // ── POST /payments/{paymentKey}/cancel ───────────────────────────

    @Test
    @DisplayName("결제 취소 성공 시 200과 TossPaymentResponse 반환")
    void cancel_success() throws Exception {
        TossPaymentResponse tossResponse = new TossPaymentResponse();
        org.springframework.test.util.ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss_pk_1234");
        org.springframework.test.util.ReflectionTestUtils.setField(tossResponse, "status", "CANCELED");

        when(paymentService.cancel(eq("toss_pk_1234"), any(PaymentCancelRequest.class))).thenReturn(tossResponse);

        mockMvc.perform(post("/payments/toss_pk_1234/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelReason\":\"고객 요청\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"))
                .andExpect(jsonPath("$.message").value("결제 취소 완료"));
    }

    @Test
    @DisplayName("PAID 아닌 주문 취소 시 400 반환")
    void cancel_notPaid_returns400() throws Exception {
        when(paymentService.cancel(anyString(), any())).thenThrow(new TossPaymentException("결제 완료 상태의 주문만 취소할 수 있습니다."));

        mockMvc.perform(post("/payments/toss_pk_1234/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelReason\":\"테스트\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 paymentKey 취소 시 404 반환")
    void cancel_notFound_returns404() throws Exception {
        when(paymentService.cancel(anyString(), any())).thenThrow(new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        mockMvc.perform(post("/payments/invalid_key/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelReason\":\"테스트\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("취소 사유 누락 시 400 반환")
    void cancel_missingReason_returns400() throws Exception {
        mockMvc.perform(post("/payments/toss_pk_1234/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelReason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /payments/fail ──────────────────────────────────────────

    @Test
    @DisplayName("결제 실패 처리 성공 시 200과 FAILED 상태 반환")
    void fail_success() throws Exception {
        PaymentFailResponse response = PaymentFailResponse.builder()
                .orderId(1L)
                .paymentStatus("FAILED")
                .build();

        when(paymentService.fail(any(PaymentFailRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"toss-uuid-001\",\"code\":\"PAY_PROCESS_CANCELED\",\"message\":\"취소\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.paymentStatus").value("FAILED"))
                .andExpect(jsonPath("$.message").value("결제 실패 처리 완료"));
    }

    @Test
    @DisplayName("존재하지 않는 tossOrderId로 실패 처리 시 404 반환")
    void fail_notFound_returns404() throws Exception {
        when(paymentService.fail(any())).thenThrow(new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        mockMvc.perform(post("/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"invalid\",\"code\":\"PAY_PROCESS_CANCELED\",\"message\":\"취소\"}"))
                .andExpect(status().isNotFound());
    }

    // ── GET /payments/orders/{orderId} ───────────────────────────────

    @Test
    @DisplayName("주문별 결제 정보 조회 성공 시 200 반환")
    void getPaymentByOrder_success() throws Exception {
        PaymentByOrderResponse response = PaymentByOrderResponse.builder()
                .paymentKey("toss_pk_1234")
                .method("카드")
                .status("DONE")
                .totalAmount(10000L)
                .paidAt(LocalDateTime.of(2025, 5, 1, 12, 0))
                .build();

        when(paymentService.getPaymentByOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/payments/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentKey").value("toss_pk_1234"))
                .andExpect(jsonPath("$.data.method").value("카드"))
                .andExpect(jsonPath("$.data.status").value("DONE"))
                .andExpect(jsonPath("$.data.totalAmount").value(10000));
    }

    @Test
    @DisplayName("권한 없는 사용자가 주문 결제 조회 시 403 반환")
    void getPaymentByOrder_forbidden_returns403() throws Exception {
        when(paymentService.getPaymentByOrder(1L)).thenThrow(new ForbiddenException("조회 권한이 없습니다."));

        mockMvc.perform(get("/payments/orders/1"))
                .andExpect(status().isForbidden());
    }

    // ── GET /payments/{paymentKey} ───────────────────────────────────

    @Test
    @DisplayName("결제 단건 조회 성공 시 200 반환")
    void getPaymentByKey_success() throws Exception {
        PaymentDetailResponse response = PaymentDetailResponse.builder()
                .paymentKey("toss_pk_1234")
                .orderId(1L)
                .method("카드")
                .status("DONE")
                .totalAmount(10000L)
                .approvedAt(LocalDateTime.of(2025, 5, 1, 12, 0))
                .requestedAt(LocalDateTime.of(2025, 5, 1, 11, 58))
                .build();

        when(paymentService.getPaymentByKey("toss_pk_1234")).thenReturn(response);

        mockMvc.perform(get("/payments/toss_pk_1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentKey").value("toss_pk_1234"))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(10000));
    }

    @Test
    @DisplayName("존재하지 않는 paymentKey 조회 시 404 반환")
    void getPaymentByKey_notFound_returns404() throws Exception {
        when(paymentService.getPaymentByKey("invalid_key")).thenThrow(new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        mockMvc.perform(get("/payments/invalid_key"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("타인 결제 단건 조회 시 403 반환")
    void getPaymentByKey_forbidden_returns403() throws Exception {
        when(paymentService.getPaymentByKey("toss_pk_1234")).thenThrow(new ForbiddenException("조회 권한이 없습니다."));

        mockMvc.perform(get("/payments/toss_pk_1234"))
                .andExpect(status().isForbidden());
    }
}
