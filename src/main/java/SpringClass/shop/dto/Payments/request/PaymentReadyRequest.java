package SpringClass.shop.dto.Payments.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentReadyRequest {

    @NotNull(message = "orderId는 필수입니다.")
    private Long orderId;
}
