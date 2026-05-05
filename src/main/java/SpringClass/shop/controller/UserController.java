package SpringClass.shop.controller;
import SpringClass.shop.dto.Products.response.ProductListDTO;
import SpringClass.shop.dto.Reviews.response.ReviewListDTO;
import SpringClass.shop.dto.Sellers.response.SellerListDTO;
import SpringClass.shop.dto.Sellers.response.SellerResponse;
import SpringClass.shop.dto.Users.request.SignupRequest;
import SpringClass.shop.dto.Users.request.SocialSignupRequest;
import SpringClass.shop.dto.Users.request.UserPasswordDTO;
import SpringClass.shop.dto.Users.request.UserProfileRequest;
import SpringClass.shop.dto.Users.response.UserProfileResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> signup
            (@RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다."));
    }

    @PostMapping("/social-signup")
    @Operation(summary = "소셜 로그인 추가 정보 입력", description = "소셜 로그인 최초 가입 시 닉네임·성별을 입력하는 API 입니다. 소셜 로그인 후 발급된 JWT를 Authorization 헤더에 포함해야 합니다.")
    public ResponseEntity<ApiResponse<Void>> completeSocialSignup(
            @RequestBody SocialSignupRequest request) {
        userService.completeSocialSignup(request);
        return ResponseEntity.ok(ApiResponse.ok("추가 정보 입력이 완료되었습니다."));
    }

    @GetMapping("/profile")
    @Operation(summary = "내 프로필 조회", description = "프로필 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        UserProfileResponse result = userService.getProfile();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PutMapping("/profile/password")
    @Operation(summary = "비밀번호 수정", description = "비밀번호 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<String>> patchPassword(@RequestBody UserPasswordDTO request) {
        String result = userService.patchPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/profile")
    @Operation(summary = "내 프로필 수정", description = "프로필 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> patchProfile(@RequestBody UserProfileRequest request) {
        UserProfileResponse result = userService.patchProfile(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @GetMapping("/likes/products")
    @Operation(summary = "내 좋아요한 상품 조회", description = "좋아요한 상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getLikeProducts
            (@RequestParam String sort) {
        List<ProductListDTO> result = userService.getLikeProducts(sort);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/wish/products")
    @Operation(summary = "내 찜한 상품 조회", description = "찜한 상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getWishProducts() {
        List<ProductListDTO> result = userService.getWishProducts();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/likes/sellers")
    @Operation(summary = "내 좋아요한 상점 조회", description = "좋아요한 상점 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<SellerListDTO>>> getLikeSellers() {
        List<SellerListDTO> result = userService.getLikeSellers();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("follow/sellers")
    @Operation(summary = "내 팔로우한 상점 조회", description = "팔로우한 상점 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<SellerListDTO>>> getFollowSellers() {
        List<SellerListDTO> result = userService.getFollowSellers();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/reviews")
    @Operation(summary = "내가 쓴 리뷰 조회", description = "내 리뷰 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ReviewListDTO>>> getMyReviews() {
        List<ReviewListDTO> result = userService.getMyReviews();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/products")
    @Operation(summary = "내 상품 조회", description = "내 상품 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getMyProducts() {
        List<ProductListDTO> result = userService.getMyProducts();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/sellers")
    @Operation(summary = "내 상점 조회", description = "내 상점 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SellerResponse>> getMySeller() {
        SellerResponse result = userService.getMySeller();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }


}
