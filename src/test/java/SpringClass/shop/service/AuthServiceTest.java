package SpringClass.shop.service;

import SpringClass.shop.dto.Auth.request.AutoLoginRequest;
import SpringClass.shop.dto.Auth.request.LoginRequest;
import SpringClass.shop.dto.Auth.request.SendEmailDto;
import SpringClass.shop.dto.Auth.request.VerifyEmailRequest;
import SpringClass.shop.dto.Auth.response.TokenResponse;
import SpringClass.shop.entity.Users.RefreshToken;
import SpringClass.shop.entity.Users.Users;
import SpringClass.shop.enums.GenderRole;
import SpringClass.shop.enums.UserRole;
import SpringClass.shop.exceptions.user.InvalidVerificationCodeException;
import SpringClass.shop.exceptions.user.RefreshTokenNotFoundException;
import SpringClass.shop.global.TokenProvider;
import SpringClass.shop.repository.Users.RefreshTokenRepository;
import SpringClass.shop.repository.Users.UsersRepository;
import SpringClass.shop.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.services.ses.SesClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsersRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenProvider tokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private SecurityUtils SecurityUtils;
    @Mock private SesClient sesClient;
    @Mock private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks private AuthService authService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L).email("test@test.com").password("encoded-pw")
                .nickname("테스터").gender(GenderRole.M).userRole(UserRole.USER).build();
    }

    @Test
    @DisplayName("로그인 성공 시 accessToken, refreshToken 반환")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        when(tokenProvider.createToken("test@test.com")).thenReturn("access-token");
        when(tokenProvider.createRefreshToken("test@test.com")).thenReturn("refresh-token");

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 BadCredentialsException 발생")
    void login_badCredentials_throws() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 autoLogin 시 새 토큰 반환")
    void autoLogin_validToken_returnsNewTokens() {
        AutoLoginRequest request = new AutoLoginRequest("valid-refresh");
        when(refreshTokenRepository.findById("valid-refresh"))
                .thenReturn(Optional.of(new RefreshToken("valid-refresh", LocalDateTime.now().plusDays(7), "test@test.com")));
        when(tokenProvider.getEmail("valid-refresh")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tokenProvider.createToken("test@test.com")).thenReturn("new-access");
        when(tokenProvider.createRefreshToken("test@test.com")).thenReturn("new-refresh");

        TokenResponse response = authService.autoLogin(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRepository).deleteById("valid-refresh");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 autoLogin 시 예외 발생")
    void autoLogin_invalidToken_throwsException() {
        AutoLoginRequest request = new AutoLoginRequest("invalid-refresh");
        when(refreshTokenRepository.findById("invalid-refresh")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.autoLogin(request))
                .isInstanceOf(RefreshTokenNotFoundException.class);
    }

    @Test
    @DisplayName("로그아웃 시 이메일로 리프레시 토큰 삭제")
    void logOut_deletesRefreshToken() {
        when(SecurityUtils.getCurrentUser()).thenReturn(user);

        authService.logOut();

        verify(refreshTokenRepository).deleteByEmail("test@test.com");
    }

    @Test
    @DisplayName("이메일 발송 시 인증 코드 저장 및 SES 호출")
    void sendEmail_savesCodeAndCallsSes() {
        SendEmailDto request = new SendEmailDto("test@test.com");

        authService.sendEmail(request);

        verify(verificationCodeRepository).deleteByEmail("test@test.com");
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        verify(sesClient).sendEmail(any(software.amazon.awssdk.services.ses.model.SendEmailRequest.class));
    }

    @Test
    @DisplayName("유효한 인증 코드로 verifyEmail 성공 및 isUsed=true")
    void verifyEmail_validCode_marksAsUsed() {
        VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "123456");
        VerificationCode code = VerificationCode.builder()
                .email("test@test.com").code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        when(verificationCodeRepository.findValidCodes(eq("test@test.com"), any(LocalDateTime.class)))
                .thenReturn(List.of(code));

        authService.verifyEmail(request);

        assertThat(code.isUsed()).isTrue();
    }

    @Test
    @DisplayName("유효한 인증 코드가 없을 때 InvalidVerificationCodeException 발생")
    void verifyEmail_noValidCodes_throwsException() {
        VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "000000");
        when(verificationCodeRepository.findValidCodes(eq("test@test.com"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> authService.verifyEmail(request))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    @DisplayName("인증 코드 불일치 시 InvalidVerificationCodeException 발생")
    void verifyEmail_wrongCode_throwsException() {
        VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "999999");
        VerificationCode code = VerificationCode.builder()
                .email("test@test.com").code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        when(verificationCodeRepository.findValidCodes(eq("test@test.com"), any(LocalDateTime.class)))
                .thenReturn(List.of(code));

        assertThatThrownBy(() -> authService.verifyEmail(request))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessage("인증 코드가 일치하지 않습니다.");
    }
}
