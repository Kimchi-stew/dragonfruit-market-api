package SpringClass.shop.dto.Payments.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentReadyResponse {
    private String tossOrderId;
    private String orderName;
    private Long amount;
    private String customerName;
}
