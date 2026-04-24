package SpringClass.shop.controller;

import SpringClass.shop.dto.Auth.request.AutoLoginRequest;
import SpringClass.shop.dto.Auth.request.LoginRequest;
import SpringClass.shop.dto.Auth.request.SendEmailRequest;
import SpringClass.shop.dto.Auth.request.VerifyEmailRequest;
import SpringClass.shop.dto.Auth.response.TokenResponse;
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
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

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

    @PostMapping("/send-email")
    @Operation(summary = "이메일 인증 코드 발송", description = "이메일에 인증 코드 발송 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@RequestBody SendEmailRequest request) {
        authService.sendEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드가 발송되었습니다."));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증 코드 확인", description = "이메일 인증 코드 확인 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("인증되었습니다."));
    }

}
