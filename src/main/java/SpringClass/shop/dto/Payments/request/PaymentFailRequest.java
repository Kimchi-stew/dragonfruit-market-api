package SpringClass.shop.dto.Payments.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentFailRequest {

    @NotBlank(message = "orderId는 필수입니다.")
    private String orderId; // tossOrderId

    @NotBlank(message = "오류 코드는 필수입니다.")
    private String code;

    @NotBlank(message = "오류 메시지는 필수입니다.")
    private String message;
}
