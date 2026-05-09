package SpringClass.shop.controller;

import SpringClass.shop.dto.Payments.request.PaymentCancelRequest;
import SpringClass.shop.dto.Payments.request.PaymentConfirmRequest;
import SpringClass.shop.dto.Payments.request.PaymentFailRequest;
import SpringClass.shop.dto.Payments.request.PaymentReadyRequest;
import SpringClass.shop.dto.Payments.response.*;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    @Operation(summary = "결제 준비", description = "토스 결제창 띄우기 전 호출. tossOrderId와 orderName 생성 후 반환.")
    public ResponseEntity<ApiResponse<PaymentReadyResponse>> ready(
            @RequestBody @Valid PaymentReadyRequest request) {
        PaymentReadyResponse response = paymentService.ready(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 준비 완료"));
    }

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "토스 결제 승인 API를 호출하고 결과를 반환합니다.")
    public ResponseEntity<ApiResponse<TossPaymentResponse>> confirm(
            @RequestBody @Valid PaymentConfirmRequest request) {
        TossPaymentResponse response = paymentService.confirm(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 승인 완료"));
    }

    @PostMapping("/{paymentKey}/cancel")
    @Operation(summary = "결제 취소", description = "토스 결제 취소 API를 호출합니다. cancelAmount 생략 시 전체 취소.")
    public ResponseEntity<ApiResponse<TossPaymentResponse>> cancel(
            @PathVariable String paymentKey,
            @RequestBody @Valid PaymentCancelRequest request) {
        TossPaymentResponse response = paymentService.cancel(paymentKey, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 취소 완료"));
    }

    @PostMapping("/fail")
    @Operation(summary = "결제 실패 처리", description = "토스 결제창에서 결제 실패/취소 시 프론트가 호출.")
    public ResponseEntity<ApiResponse<PaymentFailResponse>> fail(
            @RequestBody @Valid PaymentFailRequest request) {
        PaymentFailResponse response = paymentService.fail(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 실패 처리 완료"));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "주문별 결제 정보 조회", description = "특정 주문의 결제 정보 조회. 본인 주문 또는 해당 주문 상품의 판매자만 조회 가능.")
    public ResponseEntity<ApiResponse<PaymentByOrderResponse>> getPaymentByOrder(
            @PathVariable Long orderId) {
        PaymentByOrderResponse response = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 정보 조회 완료"));
    }

    @GetMapping("/{paymentKey}")
    @Operation(summary = "결제 단건 조회", description = "paymentKey로 결제 상세 정보 조회. 본인 결제 건 또는 ADMIN만 조회 가능.")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentByKey(
            @PathVariable String paymentKey) {
        PaymentDetailResponse response = paymentService.getPaymentByKey(paymentKey);
        return ResponseEntity.ok(ApiResponse.ok(response, "결제 단건 조회 완료"));
    }
}
