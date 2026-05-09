package SpringClass.shop.dto.Payments.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentByOrderResponse {
    private String paymentKey;
    private String method;
    private String status;
    private Long totalAmount;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
}
