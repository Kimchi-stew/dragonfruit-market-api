package SpringClass.shop.service;

import SpringClass.shop.dto.Coupon.request.CouponCreateRequest;
import SpringClass.shop.dto.Coupon.request.CouponRegisterRequest;
import SpringClass.shop.dto.Coupon.response.CouponResponse;
import SpringClass.shop.dto.Coupon.response.UserCouponResponse;
import SpringClass.shop.entity.Coupon.Coupons;
import SpringClass.shop.entity.Coupon.UserCoupons;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.DiscountType;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.common.AdminPermissionRequiredException;
import SpringClass.shop.exceptions.coupon.CouponAlreadyRegisteredException;
import SpringClass.shop.exceptions.coupon.CouponExpiredException;
import SpringClass.shop.exceptions.coupon.CouponNotFoundException;
import SpringClass.shop.repository.Coupon.CouponsRepository;
import SpringClass.shop.repository.Coupon.UserCouponsRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private CouponsRepository couponsRepository;
    @Mock private UserCouponsRepository userCouponsRepository;

    @InjectMocks private CouponService couponService;

    private Users adminUser;
    private Users normalUser;
    private Coupons coupon;

    @BeforeEach
    void setUp() {
        adminUser = Users.builder()
                .id(1L).email("admin@t.com").nickname("관리자")
                .gender(GenderRole.M).userRole(UserRole.ADMIN).build();

        normalUser = Users.builder()
                .id(2L).email("user@t.com").nickname("유저")
                .gender(GenderRole.M).userRole(UserRole.USER).build();

        coupon = Coupons.builder()
                .id(1L).code("SAVE10").discountType(DiscountType.RATE)
                .discountValue(new BigDecimal("10")).minOrderPrice(new BigDecimal("5000"))
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
    }

    @Test
    @DisplayName("관리자가 쿠폰 생성 성공")
    void createCoupon_adminUser_success() {
        CouponCreateRequest request = CouponCreateRequest.builder()
                .code("SAVE10").discountType(DiscountType.RATE)
                .discountValue(new BigDecimal("10")).minOrderPrice(new BigDecimal("5000"))
                .expiresAt(LocalDateTime.now().plusDays(7)).build();

        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(couponsRepository.existsByCode("SAVE10")).thenReturn(false);
        when(couponsRepository.save(any())).thenReturn(coupon);

        CouponResponse response = couponService.createCoupon(request);

        assertThat(response.getCode()).isEqualTo("SAVE10");
        verify(couponsRepository).save(any(Coupons.class));
    }

    @Test
    @DisplayName("일반 유저가 쿠폰 생성 시 AdminPermissionRequiredException 발생")
    void createCoupon_notAdmin_throwsException() {
        CouponCreateRequest request = CouponCreateRequest.builder()
                .code("SAVE10").discountType(DiscountType.RATE)
                .discountValue(new BigDecimal("10"))
                .expiresAt(LocalDateTime.now().plusDays(7)).build();

        when(securityUtils.getCurrentUser()).thenReturn(normalUser);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(AdminPermissionRequiredException.class);
    }

    @Test
    @DisplayName("중복된 쿠폰 코드로 생성 시 CouponAlreadyRegisteredException 발생")
    void createCoupon_duplicateCode_throwsException() {
        CouponCreateRequest request = CouponCreateRequest.builder()
                .code("SAVE10").discountType(DiscountType.RATE)
                .discountValue(new BigDecimal("10"))
                .expiresAt(LocalDateTime.now().plusDays(7)).build();

        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(couponsRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(CouponAlreadyRegisteredException.class);
    }

    @Test
    @DisplayName("쿠폰 등록 성공")
    void registerCoupon_success() {
        CouponRegisterRequest request = new CouponRegisterRequest("SAVE10");
        UserCoupons userCoupon = UserCoupons.builder()
                .id(1L).user(normalUser).coupon(coupon).isUsed(false).build();

        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(couponsRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
        when(userCouponsRepository.existsByUserAndCoupon(normalUser, coupon)).thenReturn(false);
        when(userCouponsRepository.save(any())).thenReturn(userCoupon);

        UserCouponResponse response = couponService.registerCoupon(request);

        assertThat(response.getCode()).isEqualTo("SAVE10");
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 코드로 등록 시 CouponNotFoundException 발생")
    void registerCoupon_notFound_throwsException() {
        CouponRegisterRequest request = new CouponRegisterRequest("INVALID");
        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(couponsRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.registerCoupon(request))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    @DisplayName("만료된 쿠폰 등록 시 CouponExpiredException 발생")
    void registerCoupon_expired_throwsException() {
        CouponRegisterRequest request = new CouponRegisterRequest("OLD10");
        Coupons expiredCoupon = Coupons.builder()
                .id(2L).code("OLD10").discountType(DiscountType.RATE)
                .discountValue(new BigDecimal("10"))
                .expiresAt(LocalDateTime.now().minusDays(1)).build();

        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(couponsRepository.findByCode("OLD10")).thenReturn(Optional.of(expiredCoupon));

        assertThatThrownBy(() -> couponService.registerCoupon(request))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    @DisplayName("이미 등록된 쿠폰 재등록 시 CouponAlreadyRegisteredException 발생")
    void registerCoupon_alreadyRegistered_throwsException() {
        CouponRegisterRequest request = new CouponRegisterRequest("SAVE10");
        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(couponsRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
        when(userCouponsRepository.existsByUserAndCoupon(normalUser, coupon)).thenReturn(true);

        assertThatThrownBy(() -> couponService.registerCoupon(request))
                .isInstanceOf(CouponAlreadyRegisteredException.class);
    }

    @Test
    @DisplayName("isUsed null이면 전체 쿠폰 반환")
    void getMyCoupons_nullFilter_returnsAll() {
        UserCoupons uc = UserCoupons.builder().id(1L).user(normalUser).coupon(coupon).isUsed(false).build();
        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(userCouponsRepository.findByUser(normalUser)).thenReturn(List.of(uc));

        List<UserCouponResponse> result = couponService.getMyCoupons(null);

        assertThat(result).hasSize(1);
        verify(userCouponsRepository).findByUser(normalUser);
        verify(userCouponsRepository, never()).findByUserAndIsUsed(any(), anyBoolean());
    }

    @Test
    @DisplayName("isUsed=true이면 사용된 쿠폰만 반환")
    void getMyCoupons_filteredByUsed_returnsUsedOnly() {
        UserCoupons uc = UserCoupons.builder().id(1L).user(normalUser).coupon(coupon).isUsed(true).build();
        when(securityUtils.getCurrentUser()).thenReturn(normalUser);
        when(userCouponsRepository.findByUserAndIsUsed(normalUser, true)).thenReturn(List.of(uc));

        List<UserCouponResponse> result = couponService.getMyCoupons(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isUsed()).isTrue();
    }
}
