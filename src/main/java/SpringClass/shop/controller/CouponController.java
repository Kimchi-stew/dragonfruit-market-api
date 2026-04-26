package SpringClass.shop.controller;

import SpringClass.shop.dto.Coupon.request.CouponCreateRequest;
import SpringClass.shop.dto.Coupon.request.CouponRegisterRequest;
import SpringClass.shop.dto.Coupon.response.CouponResponse;
import SpringClass.shop.dto.Coupon.response.UserCouponResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "쿠폰 생성 (관리자)", description = "관리자가 쿠폰을 생성하는 API입니다.")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@RequestBody CouponCreateRequest request) {
        CouponResponse result = couponService.createCoupon(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(result, "쿠폰이 생성되었습니다."));
    }

    @PostMapping("/register")
    @Operation(summary = "쿠폰 등록 (소비자)", description = "소비자가 쿠폰 코드를 입력하여 등록하는 API입니다.")
    public ResponseEntity<ApiResponse<UserCouponResponse>> registerCoupon(@RequestBody CouponRegisterRequest request) {
        UserCouponResponse result = couponService.registerCoupon(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(result, "쿠폰이 등록되었습니다."));
    }

    @GetMapping("/me")
    @Operation(summary = "내 쿠폰 목록 조회", description = "보유 쿠폰 목록을 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyCoupons(
            @RequestParam(required = false) Boolean isUsed) {
        List<UserCouponResponse> result = couponService.getMyCoupons(isUsed);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }
}
