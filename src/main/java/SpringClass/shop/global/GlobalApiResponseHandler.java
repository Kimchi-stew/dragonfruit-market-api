package SpringClass.shop.global;

import SpringClass.shop.exceptions.cart.CartNotFoundException;
import SpringClass.shop.exceptions.category.CategoryNameAlreadyExistException;
import SpringClass.shop.exceptions.category.CategoryNotFoundException;
import SpringClass.shop.exceptions.common.AdminPermissionRequiredException;
import SpringClass.shop.exceptions.common.ForbiddenException;
import SpringClass.shop.exceptions.coupon.CouponAlreadyRegisteredException;
import SpringClass.shop.exceptions.coupon.CouponExpiredException;
import SpringClass.shop.exceptions.coupon.CouponNotFoundException;
import SpringClass.shop.exceptions.inquiry.InquiryNotFoundException;
import SpringClass.shop.exceptions.media.MediaNotFoundException;
import SpringClass.shop.exceptions.notification.NotificationSendException;
import SpringClass.shop.exceptions.order.InsufficientStockException;
import SpringClass.shop.exceptions.order.OrderCancelNotAllowedException;
import SpringClass.shop.exceptions.order.OrderNotFoundException;
import SpringClass.shop.exceptions.product.ProductNotFoundException;
import SpringClass.shop.exceptions.review.ReviewNotFoundException;
import SpringClass.shop.exceptions.seller.SellerNotFoundException;
import SpringClass.shop.exceptions.user.InvalidVerificationCodeException;
import SpringClass.shop.exceptions.user.NotAuthenticatedException;
import SpringClass.shop.exceptions.user.PasswordMismatchException;
import SpringClass.shop.exceptions.user.RefreshTokenNotFoundException;
import SpringClass.shop.exceptions.user.UserAlreadyExistException;
import SpringClass.shop.exceptions.user.UserNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalApiResponseHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistException(UserAlreadyExistException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.fail("이미 존재하는 유저입니다."));
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotAuthenticatedException(NotAuthenticatedException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("로그인이 필요합니다."));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("사용자를 찾을 수 없습니다."));
    }

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSellerNotFoundException(SellerNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 판매자를 찾을 수 없습니다."));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 상품을 찾을 수 없습니다."));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordMismatchException(PasswordMismatchException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.fail("비밀번호가 일치하지 않습니다."));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReviewNotFoundException(ReviewNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 리뷰를 찾을 수 없습니다."));
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFoundException(CartNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("장바구니에 상품이 없습니다."));
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("리프레시 토큰이 유효하지 않습니다."));
    }

    @ExceptionHandler(AdminPermissionRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAdminPermissionRequiredException(AdminPermissionRequiredException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("관리자 권한이 아닙니다."));
    }

    @ExceptionHandler(CategoryNameAlreadyExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNameAlreadyExistException(CategoryNameAlreadyExistException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.fail("이미 존재하는 이름입니다."));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFoundException(CategoryNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("존재하지 않는 카테고리입니다."));
    }

    @ExceptionHandler(NotificationSendException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationSendException(NotificationSendException e) {
        return ResponseEntity.status(500)
                .body(ApiResponse.fail("알림이 전송 실패 했습니다."));
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCouponNotFoundException(CouponNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CouponAlreadyRegisteredException.class)
    public ResponseEntity<ApiResponse<Void>> handleCouponAlreadyRegisteredException(CouponAlreadyRegisteredException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(CouponExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleCouponExpiredException(CouponExpiredException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFoundException(OrderNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStockException(InsufficientStockException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(OrderCancelNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderCancelNotAllowedException(OrderCancelNotAllowedException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(InquiryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInquiryNotFoundException(InquiryNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaNotFoundException(MediaNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(e.getMessage()));
    }
}
