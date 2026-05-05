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
import org.springframework.web.bind.annotation.*;

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


    // 스웨거 표시용 (실제 처리는 Spring Security oauth2Login이 담당)
    @Operation(
        summary = "카카오 로그인",
        description = "이 URL로 브라우저를 이동시키면 카카오 로그인 페이지로 리다이렉트됩니다. " +
                      "로그인 완료 후 response는 일반 로그인과 동일하게 accessToken, refreshToken이 반환됩니다. " +
                      "최초 로그인(신규 가입)이면 nickname/gender가 null로 반환되므로, " +
                      "POST /users/social-signup 으로 추가 정보를 입력해야 합니다."
    )
    @GetMapping("/oauth2/authorization/kakao")
    public void kakaoLoginDoc() {}

    @Operation(
        summary = "네이버 로그인",
        description = "이 URL로 브라우저를 이동시키면 네이버 로그인 페이지로 리다이렉트됩니다. " +
                      "로그인 완료 후 response는 일반 로그인과 동일하게 accessToken, refreshToken이 반환됩니다. " +
                      "최초 로그인(신규 가입)이면 nickname/gender가 null로 반환되므로, " +
                      "POST /users/social-signup 으로 추가 정보를 입력해야 합니다."
    )
    @GetMapping("/oauth2/authorization/naver")
    public void naverLoginDoc() {}

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
