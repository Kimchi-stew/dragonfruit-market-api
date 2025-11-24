package SpringClass.shop.controller;

import SpringClass.shop.dto.Auth.AutoLoginRequest;
import SpringClass.shop.dto.Auth.LoginRequest;
import SpringClass.shop.dto.Users.SignupRequest;
import SpringClass.shop.dto.Auth.TokenResponse;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> signup
            (@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody LoginRequest request) {
        TokenResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "로그인이 완료되었습니다."));
    }

    @PostMapping("/auto-login")
    @Operation(summary = "자동 로그인", description = "자동 로그인 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<TokenResponse>> autoLogin
            (@RequestBody AutoLoginRequest request) {
        TokenResponse result = authService.autoLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "로그인이 완료되었습니다."));
    }

    @PostMapping("/log-out")
    @Operation(summary = "로그아웃", description = "로그아웃 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> logOut() {
        authService.logOut();
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 되었습니다."));
    }

}
