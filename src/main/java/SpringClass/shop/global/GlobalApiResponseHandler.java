package SpringClass.shop.global;

import SpringClass.shop.exceptions.*;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalApiResponseHandler {

    // 회원가입 시 이미 존재하는 유저 처리
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistException(UserAlreadyExistException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.fail("이미 존재하는 유저입니다."));
    }

    // 로그인이 안됀 유저 처리
    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotAuthenticatedException(NotAuthenticatedException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("로그인이 필요합니다."));
    }

    // 존재하지 않는 유저 처리
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("사용자를 찾을 수 없습니다."));
    }

    // 존재하지 않는 판매자(상점) 처리
    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSellerNotFoundException(SellerNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 판매자를 찾을 수 없습니다."));
    }

    // 존재하지 않는 상품 처리
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 상품을 찾을 수 없습니다."));
    }

    // 수정할 수 있는 권한 처리
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("수정할 수 있는 권한이 없습니다."));
    }

    // 비밀번호가 일치하지 않을 때 처리
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordMismatchException(PasswordMismatchException e) {
        return ResponseEntity.status(401)
                .body(ApiResponse.fail("비밀번호가 일치하지 않습니다."));
    }
    // 존재하지 않는 리뷰 처리
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReviewNotFoundException(ReviewNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("해당 리뷰를 찾을 수 없습니다."));
    }

    // 장바구니에 상품이 없을 때 처리
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFoundException(CartNotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("장바구니에 상품이 없습니다."));
    }

    // 리프레시 토큰이 없거나 유효하지 않을 때 처리
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("리프레시 토큰이 유효하지 않습니다."));
    }

    // 관리자 권한이 아닐 때 처리
    @ExceptionHandler(AdminPermissionRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAdminPermissionRequiredException(AdminPermissionRequiredException e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail("관리자 권한이 아닙니다."));
    }




}
