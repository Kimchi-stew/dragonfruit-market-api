package SpringClass.shop.dto.Coupon.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CouponRegisterRequest {
    private String code;
}
