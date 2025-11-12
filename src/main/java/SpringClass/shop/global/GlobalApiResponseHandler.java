package SpringClass.shop.global;

import SpringClass.shop.exceptions.NotAuthenticatedException;
import SpringClass.shop.exceptions.SellerNotFoundException;
import SpringClass.shop.exceptions.UserAlreadyExistException;
import SpringClass.shop.exceptions.UserNotFoundException;
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
                .body(ApiResponse.fail("판매자를 찾을 수 없습니다."));
    }


}
