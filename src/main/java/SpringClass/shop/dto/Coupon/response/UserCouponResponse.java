package SpringClass.shop.dto.Coupon.response;

import SpringClass.shop.entity.Coupon.UserCoupons;
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
public class UserCouponResponse {
    private Long userCouponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderPrice;
    private LocalDateTime expiresAt;
    private boolean isUsed;

    public static UserCouponResponse from(UserCoupons userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getId())
                .code(userCoupon.getCoupon().getCode())
                .discountType(userCoupon.getCoupon().getDiscountType())
                .discountValue(userCoupon.getCoupon().getDiscountValue())
                .minOrderPrice(userCoupon.getCoupon().getMinOrderPrice())
                .expiresAt(userCoupon.getCoupon().getExpiresAt())
                .isUsed(userCoupon.isUsed())
                .build();
    }
}
