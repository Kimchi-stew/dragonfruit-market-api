package SpringClass.shop.service;

import SpringClass.shop.dto.Coupon.request.CouponCreateRequest;
import SpringClass.shop.dto.Coupon.request.CouponRegisterRequest;
import SpringClass.shop.dto.Coupon.response.CouponResponse;
import SpringClass.shop.dto.Coupon.response.UserCouponResponse;
import SpringClass.shop.entity.Coupon.Coupons;
import SpringClass.shop.entity.Coupon.UserCoupons;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.AdminPermissionRequiredException;
import SpringClass.shop.exceptions.coupon.CouponAlreadyRegisteredException;
import SpringClass.shop.exceptions.coupon.CouponExpiredException;
import SpringClass.shop.exceptions.coupon.CouponNotFoundException;
import SpringClass.shop.repository.Coupon.CouponsRepository;
import SpringClass.shop.repository.Coupon.UserCouponsRepository;
import SpringClass.shop.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final SecurityUtils securityUtils;
    private final CouponsRepository couponsRepository;
    private final UserCouponsRepository userCouponsRepository;

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (user.getUserRole() != UserRole.ADMIN) {
            throw new AdminPermissionRequiredException("관리자만 쿠폰을 생성할 수 있습니다.");
        }

        if (couponsRepository.existsByCode(request.getCode())) {
            throw new CouponAlreadyRegisteredException("이미 존재하는 쿠폰 코드입니다.");
        }

        Coupons coupon = Coupons.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderPrice(request.getMinOrderPrice())
                .expiresAt(request.getExpiresAt())
                .build();

        couponsRepository.save(coupon);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public UserCouponResponse registerCoupon(CouponRegisterRequest request) {
        Users user = securityUtils.getCurrentUser();

        Coupons coupon = couponsRepository.findByCode(request.getCode())
                .orElseThrow(() -> new CouponNotFoundException("존재하지 않는 쿠폰 코드입니다."));

        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException("만료된 쿠폰입니다.");
        }

        if (userCouponsRepository.existsByUserAndCoupon(user, coupon)) {
            throw new CouponAlreadyRegisteredException("이미 등록된 쿠폰입니다.");
        }

        UserCoupons userCoupon = UserCoupons.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .build();

        userCouponsRepository.save(userCoupon);
        return UserCouponResponse.from(userCoupon);
    }

    public List<UserCouponResponse> getMyCoupons(Boolean isUsed) {
        Users user = securityUtils.getCurrentUser();

        List<UserCoupons> userCoupons;
        if (isUsed != null) {
            userCoupons = userCouponsRepository.findByUserAndIsUsed(user, isUsed);
        } else {
            userCoupons = userCouponsRepository.findByUser(user);
        }

        return userCoupons.stream()
                .map(UserCouponResponse::from)
                .collect(Collectors.toList());
    }
}
