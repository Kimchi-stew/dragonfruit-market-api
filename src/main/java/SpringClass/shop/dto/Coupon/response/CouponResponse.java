package SpringClass.shop.dto.Coupon.response;

import SpringClass.shop.entity.Coupon.Coupons;
import SpringClass.shop.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CouponResponse {
    private Long couponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiresAt;

    public static CouponResponse from(Coupons coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .expiresAt(coupon.getExpiresAt())
                .build();
    }
}
