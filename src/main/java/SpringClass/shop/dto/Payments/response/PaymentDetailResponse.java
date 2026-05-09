package SpringClass.shop.dto.Payments.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentDetailResponse {
    private String paymentKey;
    private Long orderId;
    private String method;
    private String status;
    private Long totalAmount;
    private LocalDateTime approvedAt;
    private LocalDateTime requestedAt;
}
