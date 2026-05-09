package SpringClass.shop.dto.Payments.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentFailResponse {
    private Long orderId;
    private String paymentStatus;
}
