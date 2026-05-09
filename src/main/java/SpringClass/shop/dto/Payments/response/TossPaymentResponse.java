package SpringClass.shop.dto.Payments.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long totalAmount;
    private String status;     // DONE, CANCELED, ABORTED ...
    private String method;     // 카드, 계좌이체, 가상계좌 등
    private String requestedAt;
    private String approvedAt;

    // 에러 응답 필드
    private String code;
    private String message;
}
